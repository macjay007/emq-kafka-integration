package com.mac.log.thread;

import cn.hutool.core.collection.CollectionUtil;
import com.mac.log.entity.LogInfo;
import com.mac.log.service.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
*
* @author zj
* @Date 2024/7/23 10:25
**/
public class LogQueueThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LogQueueThread.class);
    private LogInfo logInfo;
    private LogInfo logExc;
    private LogService logService;
    private List<LogInfo> logInfoList;

    public LogQueueThread(LogInfo logInfo, LogService logService) {
        this.logInfo = logInfo;
        this.logService = logService;
    }

    public LogQueueThread(LogService logService, List<LogInfo> logInfoList) {
        this.logService = logService;
        this.logInfoList = logInfoList;
    }

    public void run() {
        if (CollectionUtil.isNotEmpty(this.logInfoList)) {
            this.logService.batchInsertLogInfo(this.logInfoList);
        }

    }

    public LogInfo getLogInfo() {
        return this.logInfo;
    }

    public void setLogInfo(LogInfo logInfo) {
        this.logInfo = logInfo;
    }

    public LogInfo getLogExc() {
        return this.logExc;
    }

    public void setLogExc(LogInfo logExc) {
        this.logExc = logExc;
    }

    public List<LogInfo> getLogInfoList() {
        return this.logInfoList;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        this.logInfoList = logInfoList;
    }
}
