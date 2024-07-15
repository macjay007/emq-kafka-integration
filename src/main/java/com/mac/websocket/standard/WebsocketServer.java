package com.mac.websocket.standard;

import com.mac.websocket.pojo.PojoEndpointServer;
import com.mac.websocket.utils.SslUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class WebsocketServer {
    private final PojoEndpointServer pojoEndpointServer;
    private final ServerEndpointConfig config;
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(WebsocketServer.class);

    public WebsocketServer(PojoEndpointServer webSocketServerHandler, ServerEndpointConfig serverEndpointConfig) {
        this.pojoEndpointServer = webSocketServerHandler;
        this.config = serverEndpointConfig;
    }

    public void init() throws InterruptedException, SSLException {
        EventExecutorGroup eventExecutorGroup = null;
        final SslContext sslCtx;
        if (!StringUtils.isEmpty(this.config.getKeyStore())) {
            sslCtx = SslUtils.createSslContext(this.config.getKeyPassword(), this.config.getKeyStore(), this.config.getKeyStoreType(), this.config.getKeyStorePassword(), this.config.getTrustStore(), this.config.getTrustStoreType(), this.config.getTrustStorePassword());
        } else {
            sslCtx = null;
        }

        String[] corsOrigins = this.config.getCorsOrigins();
        Boolean corsAllowCredentials = this.config.getCorsAllowCredentials();
        final CorsConfig corsConfig = this.createCorsConfig(corsOrigins, corsAllowCredentials);
        if (this.config.isUseEventExecutorGroup()) {
            eventExecutorGroup = new DefaultEventExecutorGroup(this.config.getEventExecutorGroupThreads() == 0 ? 16 : this.config.getEventExecutorGroupThreads());
        }

        EventLoopGroup boss = new NioEventLoopGroup(this.config.getBossLoopGroupThreads());
        EventLoopGroup worker = new NioEventLoopGroup(this.config.getWorkerLoopGroupThreads());
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventExecutorGroup finalEventExecutorGroup = eventExecutorGroup;
        ((ServerBootstrap) ((ServerBootstrap) ((ServerBootstrap) ((ServerBootstrap) bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.config.getConnectTimeoutMillis())).option(ChannelOption.SO_BACKLOG, this.config.getSoBacklog())).childOption(ChannelOption.WRITE_SPIN_COUNT, this.config.getWriteSpinCount()).childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(this.config.getWriteBufferLowWaterMark(), this.config.getWriteBufferHighWaterMark())).childOption(ChannelOption.TCP_NODELAY, this.config.isTcpNodelay()).childOption(ChannelOption.SO_KEEPALIVE, this.config.isSoKeepalive()).childOption(ChannelOption.SO_LINGER, this.config.getSoLinger()).childOption(ChannelOption.ALLOW_HALF_CLOSURE, this.config.isAllowHalfClosure()).handler(new LoggingHandler(LogLevel.DEBUG))).childHandler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                if (sslCtx != null) {
                    pipeline.addFirst(new ChannelHandler[]{sslCtx.newHandler(ch.alloc())});
                }

                pipeline.addLast(new ChannelHandler[]{new HttpServerCodec()});
                pipeline.addLast(new ChannelHandler[]{new HttpObjectAggregator(65536)});
                if (corsConfig != null) {
                    pipeline.addLast(new ChannelHandler[]{new CorsHandler(corsConfig)});
                }

                pipeline.addLast(new ChannelHandler[]{new HttpServerHandler(WebsocketServer.this.pojoEndpointServer, WebsocketServer.this.config, finalEventExecutorGroup, corsConfig != null)});
            }
        });
        if (this.config.getSoRcvbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, this.config.getSoRcvbuf());
        }

        if (this.config.getSoSndbuf() != -1) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, this.config.getSoSndbuf());
        }

        ChannelFuture channelFuture;
        if ("0.0.0.0".equals(this.config.getHost())) {
            channelFuture = bootstrap.bind(this.config.getPort());
        } else {
            try {
                channelFuture = bootstrap.bind(new InetSocketAddress(InetAddress.getByName(this.config.getHost()), this.config.getPort()));
            } catch (UnknownHostException var12) {
                channelFuture = bootstrap.bind(this.config.getHost(), this.config.getPort());
                LOGGER.warn("Failed to bind to a specific network interface [{}:{}], bind to the default network interface instead", this.config.getHost(), this.config.getPort());
            }
        }

        channelFuture.addListener((future) -> {
            if (!future.isSuccess()) {
                LOGGER.error(String.format("Failed to bind to [%s:%s]", this.config.getHost(), this.config.getPort()), future.cause());
            }

        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }));
    }

    private CorsConfig createCorsConfig(String[] corsOrigins, Boolean corsAllowCredentials) {
        if (corsOrigins.length == 0) {
            return null;
        } else {
            CorsConfigBuilder corsConfigBuilder = null;
            String[] var4 = corsOrigins;
            int var5 = corsOrigins.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String corsOrigin = var4[var6];
                if ("*".equals(corsOrigin)) {
                    corsConfigBuilder = CorsConfigBuilder.forAnyOrigin();
                    break;
                }
            }

            if (corsConfigBuilder == null) {
                corsConfigBuilder = CorsConfigBuilder.forOrigins(corsOrigins);
            }

            if (corsAllowCredentials != null && corsAllowCredentials) {
                corsConfigBuilder.allowCredentials();
            }

            corsConfigBuilder.allowNullOrigin();
            return corsConfigBuilder.build();
        }
    }

    public PojoEndpointServer getPojoEndpointServer() {
        return this.pojoEndpointServer;
    }
}
