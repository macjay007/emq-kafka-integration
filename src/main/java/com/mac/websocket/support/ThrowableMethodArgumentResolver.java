package com.mac.websocket.support;

import com.mac.websocket.annotation.OnError;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

public class ThrowableMethodArgumentResolver implements MethodArgumentResolver {
    public ThrowableMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();
        if (method == null) {
            return false;
        } else {
            return method.isAnnotationPresent(OnError.class) && Throwable.class.isAssignableFrom(parameter.getParameterType());
        }
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        return object instanceof Throwable ? object : null;
    }
}
