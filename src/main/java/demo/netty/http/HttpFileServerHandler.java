package demo.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by 刘国兵 on 2016/5/17.
 */
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String url;



    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9\\.]*");

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*]");

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println("------------------------------------------------");
        if (!request.getDecoderResult().isSuccess()) {
            sendError(ctx,HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if (request.getMethod() != HttpMethod.GET) {
            sendError(ctx,HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.getUri();
        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }
        if (file.isDirectory()) {
            try {
                if (uri.endsWith("/")) {
                    sendListener(ctx,file);
                } else {
                    sendRedirect(ctx,uri + "/");
                }
            }catch (Exception e) {
                e.printStackTrace();
            }


            return;
        }
        if (!file.isFile()) {
            sendError(ctx,HttpResponseStatus.FORBIDDEN);
            return;
        }

        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile randomAccessFile ;
        try {
            randomAccessFile = new RandomAccessFile(file,"r");

        }catch (FileNotFoundException fnfe) {
            sendError(ctx,HttpResponseStatus.NOT_FOUND);
            return;
        }
        long fileLength = randomAccessFile.length();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,HttpResponseStatus.OK);
        setContentLength(response,fileLength);
        setDateAndCachedHeaders(response,file);
        setContentTypeHandler(response,file);
        System.out.println("length: " + fileLength);
        if (HttpHeaders.isKeepAlive(request)) {
            System.out.println(CONNECTION);
            response.headers().set(CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
        }
//        response.content().writeBytes("mabi".getBytes());
        ctx.write(response);//没有关闭永远无法发送过去
        System.out.println(response);
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        sendFileFuture =
                ctx.writeAndFlush(new ChunkedFile(randomAccessFile, 0, fileLength, 8192),
                        ctx.newProgressivePromise());
        //无法发送过去
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if (total < 0) {
                    System.out.println(future.channel() + "Transfer progress: " + progress);
                } else {
                    System.out.println(future.channel() + "Transfer progress: " + progress + "/" + total);

                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                System.out.println(future.channel() + "Transfer complete.");
            }
        });
        lastContentFuture = ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
        ctx.flush();
        if (!HttpHeaders.isKeepAlive(request)) {
            //关闭
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }

    private static void setDateAndCachedHeaders(HttpResponse response, File file) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
                LAST_MODIFIED, dateFormatter.format(new Date(file.lastModified())));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();

        if (ctx.channel().isActive()) {
           sendError(ctx,HttpResponseStatus.INTERNAL_SERVER_ERROR);
       }
    }

    private String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri,"ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();//转运行时异常
            }
        }
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        uri = uri.replace("/",File.separator);
        if (uri.contains(File.separator +".")
                || uri.contains("." + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        return System.getProperty("user.dir") + File.separator + uri;
    }

    private static void sendListener(ChannelHandlerContext ctx,File dir) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,HttpResponseStatus.OK);
        response.headers().set(CONTENT_TYPE,"text/html;charset=UTF-8");
        StringBuffer buf = new StringBuffer();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head>title>");
        buf.append(dirPath);
        buf.append(" 目录: ");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录： ");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf,CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,HttpResponseStatus.FOUND);
        response.headers().set(LOCATION,newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,status, Unpooled.copiedBuffer("Failure: " +status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE,"text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHandler(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypeMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE,mimeTypeMap.getContentType(file.getPath()));
    }


}
