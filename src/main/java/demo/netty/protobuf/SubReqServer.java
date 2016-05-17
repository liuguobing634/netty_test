package demo.netty.protobuf;

import demo.util.Server;
import demo.util.ServerGroups;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Created by Y on 2016/5/16.
 */
public class SubReqServer {

    public void bind(final int port) throws Exception {
        new Server().createGroups(new ServerGroups() {
            @Override
            public void group(EventLoopGroup bossGroup, EventLoopGroup workerGroup) throws Exception {
                ServerBootstrap b =  new ServerBootstrap();
                b.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG,100)
                        .handler(new LoggingHandler(LogLevel.INFO))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                //需要五个Handler
                                //中文会是Unicode字符编码，并没有处理
                                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());//半包处理
                                ch.pipeline().addLast(new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()));
                                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                                ch.pipeline().addLast(new ProtobufEncoder());//添加长度信息
                                ch.pipeline().addLast(new SubReqServerHandler());
                            }
                        });
                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            }
        });
    }


}
