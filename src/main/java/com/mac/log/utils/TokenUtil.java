package com.mac.log.utils;

import cn.hutool.core.util.ObjectUtil;
import com.mac.log.entity.UserToken;

/**
*
* @author zj
* @Date 2024/7/23 10:37
**/
public class TokenUtil {

    private static ThreadLocal<UserToken> threadLocal = new ThreadLocal<>();

    public static UserToken getCurrentUser() {
        return threadLocal.get();
    }
    public static String getCurrentUserId(){
        UserToken user = threadLocal.get();
        if (ObjectUtil.isNotNull(user)) {
            return user.getUserId();
        }
        return null;
    }
    public static String getCurrentUserName() {
        UserToken user = threadLocal.get();
        if (ObjectUtil.isNotNull(user)) {
            return user.getName();
        }
        return null;
    }
    public static String getCurrentLoginCode() {
        UserToken user = threadLocal.get();
        if (ObjectUtil.isNotNull(user)) {
            return user.getLoginCode();
        }
        return null;
    }
    public static void setCurrentUser(UserToken user) {
        threadLocal.set(user);
    }
    public static void removeCurrentUser() {
        threadLocal.remove();
    }
}
