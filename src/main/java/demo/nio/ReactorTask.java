package demo.nio;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Y on 2016/5/14.
 */
public class ReactorTask implements Runnable {

    private Selector selector;

    private volatile boolean stop;

    private ServerSocketChannel servChannel;

    //资源初始化，穿件多路复用器selector与ServerSocketChannel
    //然后注册到selector，监听SelectionKey.OP_ACCEPT操作位
    public ReactorTask(int port) {
        try{
            selector = Selector.open();
            //绑定端口并设置非阻塞模式
            servChannel = ServerSocketChannel.open();
            servChannel.
                    bind(new InetSocketAddress(port),1024);
            servChannel.configureBlocking(false);
            servChannel.register(selector,SelectionKey.OP_ACCEPT);
            System.out.println("The time server is start in port: " + port);

        } catch (IOException e) {
            e.printStackTrace();
            //出异常直接退出
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();

                    try {
                        handleInput(key);
                    }catch (Exception e) {
                        e.printStackTrace();
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }

                }
            }catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (selector != null) {
            try {
                selector.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException{
        if (key.isValid()) {
            if (key.isAcceptable()) {
                //可访问时注册读
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector,SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }

            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes,"UTF-8");
                    System.out.println("The time server receive order: " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                            new Date(System.currentTimeMillis()).toString()
                            : "BAD ORDER";
                    doWrite(sc,currentTime);
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {

                    //读到0字节，忽略
                }
            }
        }
    }

    private void doWrite(SocketChannel sc, String response)
            throws IOException{
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            //无法写回去
            sc.write(writeBuffer);
//            sc.close();//写完一次就把当前流关闭
        }
    }
}
