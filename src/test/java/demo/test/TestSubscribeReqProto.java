package demo.test;

import com.google.protobuf.InvalidProtocolBufferException;
import demo.netty.protobuf.SubscribeReqProto;


/**
 * Created by 刘国兵 on 2016/5/17.
 */
public class TestSubscribeReqProto {

    private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
        return req.toByteArray();
    }

    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static SubscribeReqProto.SubscribeReq createSubscribeReq() {
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.setSubReqID(1);
        builder.setUserName("LiuGuoBing");
        builder.setProduceName("Netty Book");
        //与书本里有些出入，书里是repeat
        builder.setAddress("EZhou HuaHu");
        return builder.build();
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode: " + req.toString());
        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
        System.out.println("After decode； " + req.toString());
        assert req2.equals(req);
    }

}
