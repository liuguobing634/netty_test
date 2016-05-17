package demo.netty.http;

import demo.util.Server;
import demo.util.ServerGroups;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.PropertyConfigurator;

/**
 * Created by 刘国兵 on 2016/5/17.
 */
public class HttpFileServer {

    private static final String DEFAULT_URL = "/src/";

    public void run(final int port, final String url) throws Exception {
        new Server().createGroups(new ServerGroups() {
            @Override
            public void group(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception {
                ServerBootstrap b = new ServerBootstrap();
                //

                b.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new HttpServerCodec());
                                ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                                ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
                                ch.pipeline().addLast("fileServerHandler",new HttpFileServerHandler(url));
                            }
                        });

                ChannelFuture f = b.bind("127.0.0.1",port).sync();
                System.out.println("HTTP 文件目录服务启动成功，网址是: " + "http://127.0.0.1:" + port + url);
                f.channel().closeFuture().sync();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        new HttpFileServer().run(8080,DEFAULT_URL);
    }

}
