package com.mac.log.utils;

import cn.hutool.core.util.ObjectUtil;
import com.mac.log.entity.BizLogInfo;

/**
*
* @author zj
* @Date 2024/7/23 10:31
**/
public class LogUtil {
    private static ThreadLocal<BizLogInfo> threadLocal = new ThreadLocal();

    public LogUtil() {
    }

    public static String getBizLogContent() {
        BizLogInfo bizLogInfo = (BizLogInfo)threadLocal.get();
        return ObjectUtil.isNotNull(bizLogInfo) ? bizLogInfo.getBizLogContent() : null;
    }

    public static void setBizLogContent(BizLogInfo content) {
        threadLocal.set(content);
    }

    public static void removeBizLogInfo() {
        threadLocal.remove();
    }
}
