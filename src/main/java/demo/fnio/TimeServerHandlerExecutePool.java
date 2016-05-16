package demo.fnio;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Y on 2016/5/14.
 */
class TimeServerHandlerExecutePool {

    private ExecutorService executor;

    TimeServerHandlerExecutePool(int maxPoolSize,int queueSize) {
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                maxPoolSize,120L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize));
    }

    void execute(Runnable task) {
        executor.execute(task);
    }
}
