package demo.netty.serializable;

import demo.util.Server;
import demo.util.ServerGroups;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
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
                                ch.pipeline().addLast(new ObjectDecoder(1024 * 1024,
                                        ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
                                ch.pipeline().addLast(new ObjectEncoder());
                                ch.pipeline().addLast(new SubReqServerHandler());
                            }
                        });
                ChannelFuture f = b.bind(port).sync();
                f.channel().closeFuture().sync();
            }
        });
    }


}
