package com.mac.websocket.support;

import com.mac.websocket.annotation.RequestParam;
import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;

import java.util.List;
import java.util.Map;

public class RequestParamMethodArgumentResolver implements MethodArgumentResolver {
    private final AbstractBeanFactory beanFactory;

    public RequestParamMethodArgumentResolver(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestParam.class);
    }

    public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
        RequestParam ann = (RequestParam)parameter.getParameterAnnotation(RequestParam.class);
        if (ann == null) {
            throw new IllegalArgumentException("RequestParam annotation not found on parameter [" + parameter.getParameterName() + "]");
        } else {
            String name = ann.name();
            if (name.isEmpty()) {
                name = parameter.getParameterName();
                if (name == null) {
                    throw new IllegalArgumentException("Name for argument type [" + parameter.getNestedParameterType().getName() + "] not available, and parameter name information not found in class file either.");
                }
            }

            if (!channel.hasAttr(PojoEndpointServer.REQUEST_PARAM)) {
                QueryStringDecoder decoder = new QueryStringDecoder(((FullHttpRequest)object).uri());
                channel.attr(PojoEndpointServer.REQUEST_PARAM).set(decoder.parameters());
            }

            Map<String, List<String>> requestParams = (Map)channel.attr(PojoEndpointServer.REQUEST_PARAM).get();
            List<String> arg = requestParams != null ? (List)requestParams.get(name) : null;
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            if (arg == null) {
                return "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n".equals(ann.defaultValue()) ? null : typeConverter.convertIfNecessary(ann.defaultValue(), parameter.getParameterType());
            } else {
                return List.class.isAssignableFrom(parameter.getParameterType()) ? typeConverter.convertIfNecessary(arg, parameter.getParameterType()) : typeConverter.convertIfNecessary(arg.get(0), parameter.getParameterType());
            }
        }
    }
}
