package com.mac.websocket.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;

public interface MethodArgumentResolver {
    boolean supportsParameter(MethodParameter var1);

    @Nullable
    Object resolveArgument(MethodParameter var1, Channel var2, Object var3) throws Exception;
}
