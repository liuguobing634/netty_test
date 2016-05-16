package demo.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y on 2016/5/16.
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(EchoServerHandler.class);

    int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String body = (String) msg;
        logger.info("This is " + ++counter + "time receive client: [" + body +"]");
        body += "$_";
        ByteBuf echos = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(echos);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
