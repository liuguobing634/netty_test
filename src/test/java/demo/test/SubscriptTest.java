package demo.test;

import demo.netty.serializable.SubReqClient;
import demo.netty.serializable.SubReqServer;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by 刘国兵 on 2016/5/16.
 */
public class SubscriptTest {

    @Before
    public void before() {
        PropertyConfigurator.configure("log4j.properties");
    }

    @Test
    public void testServer() throws Exception {
        new SubReqServer().bind(8080);
    }

    @Test
    public void testClient() throws Exception {
        new SubReqClient().connect(8080,"127.0.0.1");
    }

    @Test
    public void testProtoBufServer() throws Exception {
        new demo.netty.protobuf.SubReqServer().bind(8080);
    }

    @Test
    public void testProtoBufClient() throws Exception {
        new demo.netty.protobuf.SubReqClient().connect(8080,"127.0.0.1");
    }

}
