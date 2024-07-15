package com.mac.websocket.support;

import com.mac.websocket.annotation.PathVariable;
import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

public class PathVariableMapMethodArgumentResolver implements MethodArgumentResolver {
    public PathVariableMapMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        PathVariable ann = (PathVariable)parameter.getParameterAnnotation(PathVariable.class);
        return ann != null && Map.class.isAssignableFrom(parameter.getParameterType()) && !StringUtils.hasText(ann.value());
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        PathVariable ann = (PathVariable)parameter.getParameterAnnotation(PathVariable.class);
        if (ann == null) {
            throw new IllegalArgumentException("PathVariable annotation not found on parameter [" + parameter.getParameterName() + "]");
        } else {
            String name = ann.name();
            if (name.isEmpty()) {
                name = parameter.getParameterName();
                if (name == null) {
                    throw new IllegalArgumentException("Name for argument type [" + parameter.getNestedParameterType().getName() + "] not available, and parameter name information not found in class file either.");
                }
            }

            Map<String, String> uriTemplateVars = (Map)channel.attr(PojoEndpointServer.URI_TEMPLATE).get();
            return !CollectionUtils.isEmpty(uriTemplateVars) ? uriTemplateVars : Collections.emptyMap();
        }
    }
}
