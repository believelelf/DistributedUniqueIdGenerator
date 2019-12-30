package com.weiquding.id.util;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控工具类
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/30
 */
public class MonitorThreadPoolUtil implements Runnable{

    private final ThreadPoolExecutor executor;
    private final int delay;
    private volatile boolean running = true;

    public MonitorThreadPoolUtil(ThreadPoolExecutor executor, int delay){
        this.executor = executor;
        this.delay = delay;
    }

    public void shutdown(){
        this.running=false;
    }

    @Override
    public void run() {
        while (running){
            if(this.executor.isTerminated()){
                System.out.println("任务执行完成");
                break;
            }
            System.out.println(
                    String.format(
                     "[monitor] 池大小: %d, 核心数: %d, 活跃数: %d, 完成数: %d, 任务数: %d, 线程结束没: %s, 任务结束没: %s",
                            this.executor.getPoolSize(),
                            this.executor.getCorePoolSize(),
                            this.executor.getActiveCount(),
                            this.executor.getCompletedTaskCount(),
                            this.executor.getTaskCount(),
                            this.executor.isShutdown(),
                            this.executor.isTerminated()
                    )
            );
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
