package com.mac.websocket.support;

import com.mac.websocket.annotation.PathVariable;
import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.Channel;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;

import java.util.Map;

public class PathVariableMethodArgumentResolver implements MethodArgumentResolver {
    private final AbstractBeanFactory beanFactory;

    public PathVariableMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PathVariable.class);
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
            Object arg = uriTemplateVars != null ? uriTemplateVars.get(name) : null;
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
        }
    }
}
