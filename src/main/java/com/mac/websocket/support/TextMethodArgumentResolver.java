package com.mac.websocket.support;

import com.mac.websocket.annotation.OnMessage;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

public class TextMethodArgumentResolver implements MethodArgumentResolver {
    public TextMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();
        if (method == null) {
            return false;
        } else {
            return method.isAnnotationPresent(OnMessage.class) && String.class.isAssignableFrom(parameter.getParameterType());
        }
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        TextWebSocketFrame textFrame = (TextWebSocketFrame)object;
        return textFrame.text();
    }
}
