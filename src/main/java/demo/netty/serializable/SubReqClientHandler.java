package demo.netty.serializable;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by 刘国兵 on 2016/5/16.
 */
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int i = 0;i < 10;i++) {
            ctx.writeAndFlush(subReq(i));
        }
        ctx.flush();
    }

    private SubscriptReq subReq(int i) {
        SubscriptReq req = new SubscriptReq();
        req.setAddress("湖北省鄂州市花湖镇刘家村");
        req.setPhoneNumber("138xxxxxxxx");
        req.setProductName("志玲款 xx娃娃");
        req.setSubReqID(i);
        req.setUserName("LiuGuoBing");
        return req;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Receive server response: [" + msg +"]");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
