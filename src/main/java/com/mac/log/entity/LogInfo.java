package com.mac.log.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*
* @author zj
* @Date 2024/7/23 10:26
**/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogInfo implements Serializable {
    private static final long serialVersionUID = 864298500606874035L;
    private Integer id;
    private String operProject;
    private String operModul;
    private String operUri;
    private String operType;
    private String operMethod;
    private String operDesc;
    private String operRequParam;
    private String operRespParam;
    private String operUserId;
    private String operUserName;
    private String operIp;
    private Long operCreateTime;
    private String operVer;
    private Integer operResult;
    private String excName;
    private String excMessage;
}
