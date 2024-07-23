package com.mac.log.service;

import com.mac.log.entity.LogConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
*
* @author zj
* @Date 2024/7/23 10:11 
**/
@Repository
public interface LogConfigService {
    int deleteByPrimaryKey(Integer id);

    int insert(LogConfig record);

    int insertOrUpdate(LogConfig record);

    int insertOrUpdateSelective(LogConfig record);

    int insertSelective(LogConfig record);

    LogConfig selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LogConfig record);

    int updateByPrimaryKey(LogConfig record);

    int updateBatch(List<LogConfig> list);

    int updateBatchSelective(List<LogConfig> list);

    int batchInsert(  List<LogConfig> list);

    int batchInsertOrUpdate(List<LogConfig> list);

    int selectCountByUrl(String requestURI);
}
