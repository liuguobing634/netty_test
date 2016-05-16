package demo.netty.echo;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y on 2016/5/16.
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    private int counter;

    private static Logger logger = LoggerFactory.getLogger(EchoClientHandler.class);

    static final String ECHO_REQ = "Hi, LiuGuoBing. Welcome to Netty.$_";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0;i < 10;i ++) {
            ctx.writeAndFlush(Unpooled.copiedBuffer(ECHO_REQ.getBytes()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("This is "+ ++counter +" times receive server: [" + msg +"]");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
