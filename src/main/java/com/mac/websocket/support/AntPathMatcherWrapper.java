package com.mac.websocket.support;

import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.Map;

public class AntPathMatcherWrapper extends AntPathMatcher implements WsPathMatcher {
    private final String pattern;

    public AntPathMatcherWrapper(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        Map<String, String> variables = new LinkedHashMap();
        boolean result = this.doMatch(this.pattern, decoder.path(), true, variables);
        if (result) {
            channel.attr(PojoEndpointServer.URI_TEMPLATE).set(variables);
            return true;
        } else {
            return false;
        }
    }
}
