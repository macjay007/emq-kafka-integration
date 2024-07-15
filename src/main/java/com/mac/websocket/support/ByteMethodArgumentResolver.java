package com.mac.websocket.support;

import com.mac.websocket.annotation.OnBinary;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

public class ByteMethodArgumentResolver implements MethodArgumentResolver {
    protected final Log logger = LogFactory.getLog(this.getClass());

    public ByteMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        Method method = parameter.getMethod();
        if (method == null) {
            this.logger.error("Parameter method is null");
            return false;
        } else {
            return method.isAnnotationPresent(OnBinary.class) && byte[].class.isAssignableFrom(parameter.getParameterType());
        }
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame)object;
        ByteBuf content = binaryWebSocketFrame.content();
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return bytes;
    }
}
