package com.hanrx.mobilesafe.volleyhanrx.http;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private static final String TAG = "hanrx";

    private static ThreadPoolManager instance = new ThreadPoolManager();

    private LinkedBlockingDeque<Future<?>> taskQueue = new LinkedBlockingDeque<>();

    private ThreadPoolExecutor threadPoolExecutor;

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    private ThreadPoolManager() {
        threadPoolExecutor = new ThreadPoolExecutor(4,10,10,
                TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(4), handler);
        threadPoolExecutor.execute(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                FutureTask futureTask = null;
                try {
                    //阻塞式函数
                    Log.i(TAG,"等待队列   " + taskQueue.size());
                    futureTask = (FutureTask) taskQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (futureTask != null) {
                    threadPoolExecutor.execute(futureTask);
                }
                Log.i(TAG,"线程池大小   " + threadPoolExecutor.getPoolSize());
            }
        }
    };

    public <T> void execte(FutureTask<T> futureTask) throws InterruptedException{
        taskQueue.put(futureTask);
    }

    private RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
            try {
                taskQueue.put(new FutureTask(runnable, null) {

                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
