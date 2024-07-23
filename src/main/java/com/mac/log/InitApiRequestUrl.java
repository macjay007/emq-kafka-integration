package com.mac.log;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.mac.log.annotation.OperLog;
import com.mac.log.entity.LogConfig;
import com.mac.log.service.LogConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
*
* @author zj
* @Date 2024/7/23 10:31
**/
@Component
public class InitApiRequestUrl {
    @Value("${server.servlet.context-path}")
    public String contextPath;
    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    LogConfigService logConfigService;

    public InitApiRequestUrl() {
    }

    @PostConstruct
    public void init() {
        String projectName = this.contextPath.replaceAll("/", "");
        List<LogConfig> logConfigs = Lists.newArrayList();
        RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping)this.applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        Iterator var5 = map.entrySet().iterator();

        while(var5.hasNext()) {
            Map.Entry<RequestMappingInfo, HandlerMethod> mappingInfoHandlerMethodEntry = (Map.Entry)var5.next();
            RequestMappingInfo requestMappingInfo = (RequestMappingInfo)mappingInfoHandlerMethodEntry.getKey();
            HandlerMethod handlerMethod = (HandlerMethod)mappingInfoHandlerMethodEntry.getValue();
            Annotation[] annotations = handlerMethod.getMethod().getDeclaredAnnotations();
            PatternsRequestCondition p = requestMappingInfo.getPatternsCondition();
            LogConfig logConfig = new LogConfig();
            logConfig.setOperProject(projectName);
            logConfig.setCreateDate(System.currentTimeMillis());
            logConfig.setIsEnable(1);
            boolean isLogAnnotationExist = false;
            Annotation[] var13 = annotations;
            int var14 = annotations.length;

            for(int var15 = 0; var15 < var14; ++var15) {
                Annotation annotation = var13[var15];
                if (annotation instanceof OperLog) {
                    isLogAnnotationExist = true;
                    OperLog operLog = (OperLog)annotation;
                    logConfig.setOperModul(operLog.operModule());
                    logConfig.setOperDesc(operLog.operDesc());
                    logConfig.setOperType(operLog.operType());
                    break;
                }
            }

            if (isLogAnnotationExist) {
                Iterator var18 = p.getPatterns().iterator();
                if (var18.hasNext()) {
                    String url = (String)var18.next();
                    logConfig.setOperUri("/" + projectName + url);
                }

                logConfigs.add(logConfig);
            }
        }

        if (CollectionUtil.isNotEmpty(logConfigs)) {
            this.logConfigService.batchInsertOrUpdate(logConfigs);
        }

    }
}
