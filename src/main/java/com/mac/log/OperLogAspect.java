package com.mac.log;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.mac.log.annotation.OperLog;
import com.mac.log.entity.LogInfo;
import com.mac.log.service.LogConfigService;
import com.mac.log.service.LogService;
import com.mac.log.utils.IpUtils;
import com.mac.log.utils.LogUtil;
import com.mac.log.utils.TokenUtil;
import com.mac.websocket.exception.CommonException;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
*
* @author zj
* @Date 2024/7/23 10:07
**/
@Aspect
@Component
public class OperLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);

    private final LogService logService;

    private final LogConfigService logConfigService;

    @Value("${server.servlet.context-path}")
    public String contextPath;

    public OperLogAspect(LogService logService, LogConfigService logConfigService) {
        this.logService = logService;
        this.logConfigService = logConfigService;
    }

    @Pointcut("@annotation(OperLog)")
    public void operLogPointCut() {
    }

    @Pointcut("execution(public * com.*..controller..*.*(..))")
    public void operControllerPointCut() {
    }

    @AfterReturning(
        value = "operLogPointCut()",
        returning = "keys"
    )
    public void saveOperLog(JoinPoint joinPoint, Object keys) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        assert requestAttributes != null;

        HttpServletRequest request = (HttpServletRequest)requestAttributes.resolveReference("request");
        int isEnable = this.logConfigService.selectCountByUrl(request.getRequestURI());
        if (isEnable != 1) {
            LogInfo operationLog = new LogInfo();
            String userId = TokenUtil.getCurrentUserId();
            String userName = TokenUtil.getCurrentUserName();

            try {
                MethodSignature signature = (MethodSignature)joinPoint.getSignature();
                Method method = signature.getMethod();
                OperLog opLog = method.getAnnotation(OperLog.class);
                String className;
                String methodName;
                String operDesc;
                if (opLog != null) {
                    className = opLog.operModule();
                    methodName = opLog.operType();
                    operationLog.setOperModul(className);
                    operationLog.setOperType(methodName);
                    operDesc = opLog.operDesc();
                    String bizLogContent = LogUtil.getBizLogContent();
                    if (ObjectUtils.isNotEmpty(bizLogContent)) {
                        operDesc = bizLogContent;
                    }

                    operDesc = String.format("用户[%s] [id:%s]," + operDesc + ",操作成功", TokenUtil.getCurrentUserName(), TokenUtil.getCurrentUserId());
                    operationLog.setOperDesc(operDesc);
                    operationLog.setOperProject(StrUtil.removeAll(this.contextPath, "/"));
                }

                className = joinPoint.getTarget().getClass().getName();
                methodName = method.getName();
                methodName = className + "." + methodName;
                operationLog.setOperMethod(methodName);
                operDesc = this.getRequestParameter(request);
                operationLog.setOperRequParam(operDesc);
                operationLog.setOperRespParam(JSON.toJSONString(keys,  JSONWriter.Feature.WriteMapNullValue));
                operationLog.setOperUserId(userId);
                operationLog.setOperUserName(userName);

                assert request != null;

                operationLog.setOperIp(IpUtils.getIpAddr(request));
                operationLog.setOperUri(request.getRequestURI());
                operationLog.setOperCreateTime(System.currentTimeMillis());
                operationLog.setOperResult(0);
                BlockingQueue<LogInfo> queue = LogAsyncQueue.blockingInfoQueue;
                queue.put(operationLog);
                LogUtil.removeBizLogInfo();
                log.info("请求：" + operationLog);
            } catch (Exception var17) {
                LogUtil.removeBizLogInfo();
                log.error("拦截请求失败:{}", var17.getMessage());
            }

        }
    }


    @AfterThrowing(
        pointcut = "operControllerPointCut()",
        throwing = "e"
    )
    public void saveExceptionLog(JoinPoint joinPoint, Throwable e) {
        log.info("--------------进入{}异常后执行方法", "saveExceptionLog");
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        assert requestAttributes != null;

        HttpServletRequest request = (HttpServletRequest)requestAttributes.resolveReference("request");
        int isEnable = this.logConfigService.selectCountByUrl(request.getRequestURI());
        if (isEnable != 1) {
            LogInfo logExc = new LogInfo();
            String userId = TokenUtil.getCurrentUserId();
            String userName = TokenUtil.getCurrentUserName();

            try {
                MethodSignature signature = (MethodSignature)joinPoint.getSignature();
                Method method = signature.getMethod();
                String className = joinPoint.getTarget().getClass().getName();
                String methodName = method.getName();
                methodName = className + "." + methodName;
                OperLog opLog = method.getAnnotation(OperLog.class);
                if (opLog == null) {
                    return;
                }

                String operType = opLog.operType();
                String bizLogContent = LogUtil.getBizLogContent();
                String operDesc = opLog.operDesc();
                if (ObjectUtils.isNotEmpty(bizLogContent)) {
                    operDesc = bizLogContent;
                }

                operDesc = String.format("用户[%s] [id:%s]," + operDesc + ",操作失败", TokenUtil.getCurrentUserName(), TokenUtil.getCurrentUserId());
                String params = this.getRequestParameter(request);
                logExc.setOperRequParam(params);
                logExc.setOperMethod(methodName);
                logExc.setExcName(e.getClass().getName());
                CommonException commonException = null;
                if (e instanceof CommonException) {
                    commonException = (CommonException)e;
                    logExc.setExcMessage(commonException.getMessage());
                } else {
                    logExc.setExcMessage(e.getMessage());
                }

                logExc.setOperType(operType);
                logExc.setOperDesc(operDesc);
                logExc.setOperUserId(userId);
                logExc.setOperUserName(userName);
                logExc.setOperUri(request.getRequestURI());
                logExc.setOperIp(IpUtils.getIpAddr(request));
                logExc.setOperCreateTime(System.currentTimeMillis());
                logExc.setOperProject(StrUtil.removeAll(this.contextPath, "/"));
                logExc.setOperModul(opLog.operModule());
                logExc.setOperResult(1);
                BlockingQueue<LogInfo> queue = LogAsyncQueue.blockingInfoQueue;
                queue.put(logExc);
                LogUtil.removeBizLogInfo();
                log.info("请求：" + logExc);
            } catch (Exception var21) {
                LogUtil.removeBizLogInfo();
                log.error("拦截异常失败:{}", var21.getMessage());
            }

        }
    }

    public static String getRequestParameter(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = streamReader.readLine()) != null) {
                sb.append(line);
            }
            if (sb.length() != 0) {
                // 这里假设我们只需要返回JSON字符串的内容，而不是再次转换为JSON字符串
                return sb.toString();
            }
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap != null && !paramMap.isEmpty()) {
                return JSON.toJSONString(paramMap);
            }
            return "";
        } catch (IOException e) {
            log.error("读取请求参数时发生IO异常", e);
            return "";
        }
    }

}
