package com.mac.log.timer;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.mac.log.LogAsyncQueue;
import com.mac.log.entity.LogInfo;
import com.mac.log.service.LogService;
import com.mac.log.thread.LogQueueThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
*
* @author zj
* @Date 2024/7/23 10:27 
**/
@Configuration
@EnableScheduling
public class LogTimer {
    private static final Logger log = LoggerFactory.getLogger(LogTimer.class);
    private static final String LOG_MAXIMUM_NUMBER_OF_RECORDS = "log_maximum_number_of_records";
    private static final String LOG_MAXIMUM_EXPIRATION_TIME = "log_minimum_expiration_time";
    private static final String LOG_CAPACITY_CONFIGURATION = "log_capacity_configuration";
    private   final LogService logService;
    private static ThreadPoolExecutor threadPool;

    public LogTimer(LogService logService) {
        this.logService = logService;
    }

    @Scheduled(
        fixedRate = 5000L
    )
    public void saveLog() {
        BlockingQueue<LogInfo> infoQueue = LogAsyncQueue.blockingInfoQueue;
        List<LogInfo> logInfoList = Lists.newLinkedList();

        for(int i = 0; i < infoQueue.size(); ++i) {
            LogInfo logInfo = (LogInfo)infoQueue.poll();
            logInfoList.add(logInfo);
        }

        if (CollectionUtil.isNotEmpty(logInfoList)) {
            threadPool.submit(new LogQueueThread(this.logService, logInfoList));
        }

    }

    public void deleteLog() {
        int logCapacityConfiguration = this.logService.selectSysConfigValueByKey("log_capacity_configuration");
        int deleteInfoCount = 0;
        int deleteExcCount = 0;
        int value;
        if (logCapacityConfiguration == 1) {
            value = this.logService.selectSysConfigValueByKey("log_maximum_number_of_records");
            int logInfoCount = this.logService.selectLogInfoCount();
            if (logInfoCount > value) {
                deleteInfoCount = this.logService.deleteLogInfoByLimit(value, logInfoCount);
            }
        } else {
            value = this.logService.selectSysConfigValueByKey("log_minimum_expiration_time");
            deleteInfoCount = this.logService.deleteLogInfoByMonth(value);
            deleteExcCount = this.logService.deleteLogExcByMonth(value);
        }

        log.debug("删除log_info记录数：{}", deleteInfoCount);
        log.debug("删除log_exc记录数：{}", deleteExcCount);
    }

    static {
        threadPool = LogAsyncQueue.executor;
    }
}
