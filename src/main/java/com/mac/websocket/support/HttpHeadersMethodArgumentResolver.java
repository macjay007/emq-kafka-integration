package com.mac.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.core.MethodParameter;

public class HttpHeadersMethodArgumentResolver implements MethodArgumentResolver {
    public HttpHeadersMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return HttpHeaders.class.isAssignableFrom(parameter.getParameterType());
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        return ((FullHttpRequest)object).headers();
    }
}
