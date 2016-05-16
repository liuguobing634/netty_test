package demo.bio;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created by Y on 2016/5/14.
 */
public class TimeClient {

    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //采取默认值
            }
        }
        try (Socket socket = new Socket("127.0.0.1",port);
             final OutputStream out = socket.getOutputStream();
             ReadableByteChannel in = Channels.newChannel(socket.getInputStream())

        ) {
            //一个线程写
            try {
                out.write("QUERY TIME ORDER".getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Send order 2 server succeed.");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            in.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String time = new String(bytes,"UTF-8");
            System.out.println("Now is: " + time);


        } catch (Exception e){
            //不处理
            e.printStackTrace();
        }
    }

}
