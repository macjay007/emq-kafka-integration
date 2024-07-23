package com.mac.log.service;

import com.mac.log.entity.LogInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
*
* @author zj
* @Date 2024/7/23 10:10
**/
@Repository
public interface LogService {
    int insertLogInfoSelective(LogInfo logInfo);

    int batchInsertLogInfo(List<LogInfo> logInfoList);

    List<String> selectLogConfigApi(String operProject);

    int selectSysConfigValueByKey(String key);

    int selectLogInfoCount();

    int selectLogExcCount();

    int deleteLogInfoByLimit( int start,   int end);

    int deleteLogExcByLimit(  int start,  int end);

    int deleteLogInfoByMonth(  int month);

    int deleteLogExcByMonth(  int month);
}
