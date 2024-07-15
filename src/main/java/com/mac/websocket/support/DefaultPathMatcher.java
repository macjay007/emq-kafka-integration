package com.mac.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

public class DefaultPathMatcher implements WsPathMatcher {
    private final String pattern;

    public DefaultPathMatcher(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }

    public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
        return this.pattern.equals(decoder.path());
    }
}
