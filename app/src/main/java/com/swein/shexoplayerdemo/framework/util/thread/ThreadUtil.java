package com.swein.shexoplayerdemo.framework.util.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtil {

    //fixed number thread pool, can reuse
    static private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    //only one thread can run at one time
    static private ExecutorService executorSequential = Executors.newSingleThreadExecutor();

    @Override
    protected void finalize() throws Throwable {
        if (executor != null && executor.isShutdown()) {
            executor.shutdown();
        }
        if (executorSequential != null && executorSequential.isShutdown()) {
            executorSequential.shutdown();
        }
        super.finalize();
    }


    private static Handler handle = new Handler(Looper.getMainLooper());

    public static Handler startUIThread(int delayMillis, Runnable runnable) {

        handle.postDelayed(runnable, delayMillis);

        return handle;
    }

    public static Future<?> startThread(final Runnable runnable) {
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        return future;
    }

    public static Future<?> startSeqThread(final Runnable runnable) {
        Future<?> future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        return future;
    }

}
