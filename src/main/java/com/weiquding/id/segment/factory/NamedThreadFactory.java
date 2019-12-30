package com.weiquding.id.segment.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/30
 */
public class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup threadGroup;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final String namePrefix;
    private final boolean daemon;

    public NamedThreadFactory(String namePrefix, boolean daemon) {
        this.daemon = daemon;
        SecurityManager securityManager = System.getSecurityManager();
        threadGroup = (securityManager != null) ? securityManager.getThreadGroup()
                : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public NamedThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }


    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r, namePrefix + "-thread-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }
}
