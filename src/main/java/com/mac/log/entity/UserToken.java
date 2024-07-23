package com.mac.log.entity;

/**
*
* @author zj
* @Date 2024/7/23 10:38
**/
public class UserToken {
    /**
     * 用户ID db_column: user_id
     */
    private String userId;
    /**
     * 登录名 db_column: login_code
     */
    private String loginCode;

    /**
     * 姓名 db_column: name
     */
    private String name;
    /**
     * 人员ID db_column: employee_id
     */
    private String employeeId;

    /**
     * 人员姓名
     */
    private String employeeName;
    /**
     * 通行令牌
     */
    private String token;
}
