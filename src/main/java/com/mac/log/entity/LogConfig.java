package com.mac.log.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*
* @author zj
* @Date 2024/7/23 13:11 
**/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogConfig implements Serializable {
    private Integer id;
    private String operProject;
    private String operModul;
    private String operUri;
    private String operType;
    private String operDesc;
    private Long createDate;
    private String createUser;
    private Long modifyDate;
    private String modifyUser;
    private Integer isEnable;
    private String memo;
    private static final long serialVersionUID = 1L;
}
