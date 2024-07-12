package com.mac.websocket.config;

import com.mac.websocket.standard.ServerEndpointExporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnMissingBean({ServerEndpointExporter.class})
@Configuration
public class NettyWebSocketSelector {
    public NettyWebSocketSelector() {
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
