package com.mac.websocket.support;

import com.mac.websocket.annotation.RequestParam;
import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.core.MethodParameter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class RequestParamMapMethodArgumentResolver implements MethodArgumentResolver {
    public RequestParamMapMethodArgumentResolver() {
    }

    public boolean supportsParameter(MethodParameter parameter) {
        RequestParam requestParam = (RequestParam)parameter.getParameterAnnotation(RequestParam.class);
        return requestParam != null && Map.class.isAssignableFrom(parameter.getParameterType()) && !StringUtils.hasText(requestParam.name());
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
            LinkedMultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap(requestParams);
            return MultiValueMap.class.isAssignableFrom(parameter.getParameterType()) ? multiValueMap : multiValueMap.toSingleValueMap();
        }
    }
}
