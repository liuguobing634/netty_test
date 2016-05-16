package demo.nio;



/**
 * Created by Y on 2016/5/14.
 */
public class TimeServer {

    public static void main(String[] args) {
        ReactorTask task = new ReactorTask(8080);
        new Thread(task,"NIO-ReactorTask-001").start();

    }

}
