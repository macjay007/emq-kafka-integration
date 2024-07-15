package com.mac.websocket.support;

import com.mac.websocket.pojo.PojoEndpointServer;
import com.mac.websocket.pojo.Session;
import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;

public class SessionMethodArgumentResolver implements MethodArgumentResolver {
    public SessionMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return Session.class.isAssignableFrom(parameter.getParameterType());
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        return channel.attr(PojoEndpointServer.SESSION_KEY).get();
    }
}
