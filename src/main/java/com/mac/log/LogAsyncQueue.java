package com.mac.log;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mac.log.entity.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * @author: mac
 * @date: 2024/7/23
 */
public class LogAsyncQueue {
    private static final Logger log = LoggerFactory.getLogger(LogAsyncQueue.class);
    private static final int QUEUE_MAX_SIZE = 1000;
    private static final int CORE_POOL_SIZE = 1;
    private static final long KEEP_ACTIVE_TIME = 200L;
    private static final int MAX_POOL_SIZE = 10;
    private static TimeUnit timeUnit;
    public static BlockingQueue<LogInfo> blockingInfoQueue;
    public static ThreadPoolExecutor executor;
    private static ThreadFactory namedThreadFactory;

    public LogAsyncQueue() {
    }

    public static void pushInfoLog(LogInfo logInfo) throws InterruptedException {
        if (logInfo != null) {
            blockingInfoQueue.put(logInfo);
        }

    }

    public static LogInfo pollInfoLog() {
        LogInfo logInfo = null;

        try {
            logInfo = (LogInfo)blockingInfoQueue.take();
        } catch (InterruptedException var2) {
            log.error("Info消息出队失败 errMsg:{}", var2.getMessage());
        }

        return logInfo;
    }

    public static int infoLogSize() {
        return blockingInfoQueue.size();
    }

    public static int excLogSize() {
        return blockingInfoQueue.size();
    }

    @PreDestroy
    public void destroyMethod() {
        log.info("停止线程池：{}", namedThreadFactory);
        executor.shutdown();
    }

    static {
        timeUnit = TimeUnit.SECONDS;
        namedThreadFactory = (new ThreadFactoryBuilder()).setNameFormat("writeLogQueue-pool-%d").build();
        blockingInfoQueue = new LinkedBlockingQueue(1000);
        executor = new ThreadPoolExecutor(1, 10, 200L, timeUnit, new LinkedBlockingQueue(200), namedThreadFactory);
    }
}
