package demo.util;

import io.netty.channel.EventLoopGroup;

/**
 * Created by Y on 2016/5/16.
 */
public interface ServerGroups {

    void group(EventLoopGroup bossGroup,EventLoopGroup workerGroup) throws Exception;

}
