package com.mac.websocket.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

public interface WsPathMatcher {
    String getPattern();

    boolean matchAndExtract(QueryStringDecoder var1, Channel var2);
}
