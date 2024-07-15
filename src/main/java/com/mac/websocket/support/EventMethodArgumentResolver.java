package com.mac.websocket.support;

import com.mac.websocket.annotation.OnEvent;
import io.netty.channel.Channel;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

public class EventMethodArgumentResolver implements MethodArgumentResolver {
    private final AbstractBeanFactory beanFactory;

    public EventMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();
        return method == null ? false : method.isAnnotationPresent(OnEvent.class);
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        if (object == null) {
            return null;
        } else {
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            return typeConverter.convertIfNecessary(object, parameter.getParameterType());
        }
    }
}
