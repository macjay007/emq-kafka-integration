package com.mac.websocket.standard;

import com.mac.websocket.pojo.PojoEndpointServer;
import com.mac.websocket.support.WsPathMatcher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;


public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final PojoEndpointServer pojoEndpointServer;
    private final ServerEndpointConfig config;
    private final EventExecutorGroup eventExecutorGroup;
    private final boolean isCors;
    private static final ByteBuf FAVICON_BYTE_BUF = buildStaticRes("/favicon.ico");
    private static ByteBuf notFoundByteBuf = buildStaticRes("/public/error/404.html");
    private static ByteBuf badRequestByteBuf = buildStaticRes("/public/error/400.html");
    private static ByteBuf forbiddenByteBuf = buildStaticRes("/public/error/403.html");
    private static ByteBuf internalServerErrorByteBuf = buildStaticRes("/public/error/500.html");

    private static ByteBuf buildStaticRes(String resPath) {
        try {
            InputStream inputStream = HttpServerHandler.class.getResourceAsStream(resPath);
            Throwable var2 = null;

            ByteBuf var5;
            try {
                if (inputStream == null) {
                    return null;
                }

                int available = inputStream.available();
                if (available == 0) {
                    return null;
                }

                byte[] bytes = new byte[available];
                inputStream.read(bytes);
                var5 = ByteBufAllocator.DEFAULT.buffer(bytes.length).writeBytes(bytes);
            } catch (Throwable var16) {
                var2 = var16;
                throw var16;
            } finally {
                if (inputStream != null) {
                    if (var2 != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var15) {
                            var2.addSuppressed(var15);
                        }
                    } else {
                        inputStream.close();
                    }
                }

            }

            return var5;
        } catch (Exception var18) {
            return null;
        }
    }

    public HttpServerHandler(PojoEndpointServer pojoEndpointServer, ServerEndpointConfig config, EventExecutorGroup eventExecutorGroup, boolean isCors) {
        this.pojoEndpointServer = pojoEndpointServer;
        this.config = config;
        this.eventExecutorGroup = eventExecutorGroup;
        this.isCors = isCors;
    }

    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        DefaultFullHttpResponse res;
        try {
            this.handleHttpRequest(ctx, msg);
        } catch (TypeMismatchException var5) {
            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            sendHttpResponse(ctx, msg, res);
        } catch (Exception var6) {
            if (internalServerErrorByteBuf != null) {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, internalServerErrorByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }

            sendHttpResponse(ctx, msg, res);
            this.logger.error(var6);
        }

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.pojoEndpointServer.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        DefaultFullHttpResponse res;
        if (!req.decoderResult().isSuccess()) {
            if (badRequestByteBuf != null) {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, badRequestByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            }

            sendHttpResponse(ctx, req, res);
        } else if (req.method() != HttpMethod.GET) {
            if (forbiddenByteBuf != null) {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
            } else {
                res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
            }

            sendHttpResponse(ctx, req, res);
        } else {
            HttpHeaders headers = req.headers();
            String host = headers.get(HttpHeaderNames.HOST);
            if (StringUtils.isEmpty(host)) {
                if (forbiddenByteBuf != null) {
                    res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
                } else {
                    res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                }

                sendHttpResponse(ctx, req, res);
            } else if (!StringUtils.isEmpty(this.pojoEndpointServer.getHost()) && !"0.0.0.0".equals(this.pojoEndpointServer.getHost()) && !this.pojoEndpointServer.getHost().equals(host.split(":")[0])) {
                if (forbiddenByteBuf != null) {
                    res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
                } else {
                    res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                }

                sendHttpResponse(ctx, req, res);
            } else {
                QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
                String path = decoder.path();
                if ("/favicon.ico".equals(path)) {
                    if (FAVICON_BYTE_BUF != null) {
                        res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, FAVICON_BYTE_BUF.retainedDuplicate());
                    } else {
                        res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                    }

                    sendHttpResponse(ctx, req, res);
                } else {
                    Channel channel = ctx.channel();
                    String pattern = null;
                    Set<WsPathMatcher> pathMatcherSet = this.pojoEndpointServer.getPathMatcherSet();
                    Iterator var11 = pathMatcherSet.iterator();

                    while (var11.hasNext()) {
                        WsPathMatcher pathMatcher = (WsPathMatcher) var11.next();
                        if (pathMatcher.matchAndExtract(decoder, channel)) {
                            pattern = pathMatcher.getPattern();
                            break;
                        }
                    }

                    if (pattern == null) {
                        if (notFoundByteBuf != null) {
                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, notFoundByteBuf.retainedDuplicate());
                        } else {
                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                        }

                        sendHttpResponse(ctx, req, res);
                    } else if (req.headers().contains(HttpHeaderNames.UPGRADE) && req.headers().contains(HttpHeaderNames.SEC_WEBSOCKET_KEY) && req.headers().contains(HttpHeaderNames.SEC_WEBSOCKET_VERSION)) {
                        String subprotocols = null;
                        if (this.pojoEndpointServer.hasBeforeHandshake(channel, pattern)) {
                            this.pojoEndpointServer.doBeforeHandshake(channel, req, pattern);
                            if (!channel.isActive()) {
                                return;
                            }

                            AttributeKey<String> subprotocolsAttrKey = AttributeKey.valueOf("subprotocols");
                            if (channel.hasAttr(subprotocolsAttrKey)) {
                                subprotocols = (String) ctx.channel().attr(subprotocolsAttrKey).get();
                            }
                        }

                        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), subprotocols, true, this.config.getmaxFramePayloadLength());
                        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
                        if (handshaker == null) {
                            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
                        } else {
                            ChannelPipeline pipeline = ctx.pipeline();
                            pipeline.remove(ctx.name());
                            if (this.config.getReaderIdleTimeSeconds() != 0 || this.config.getWriterIdleTimeSeconds() != 0 || this.config.getAllIdleTimeSeconds() != 0) {
                                pipeline.addLast(new ChannelHandler[]{new IdleStateHandler(this.config.getReaderIdleTimeSeconds(), this.config.getWriterIdleTimeSeconds(), this.config.getAllIdleTimeSeconds())});
                            }

                            if (this.config.isUseCompressionHandler()) {
                                pipeline.addLast(new ChannelHandler[]{new WebSocketServerCompressionHandler()});
                            }

                            pipeline.addLast(new ChannelHandler[]{new WebSocketFrameAggregator(Integer.MAX_VALUE)});
                            if (this.config.isUseEventExecutorGroup()) {
                                pipeline.addLast(this.eventExecutorGroup, new ChannelHandler[]{new WebSocketServerHandler(this.pojoEndpointServer)});
                            } else {
                                pipeline.addLast(new ChannelHandler[]{new WebSocketServerHandler(this.pojoEndpointServer)});
                            }

                            String finalPattern = pattern;
                            handshaker.handshake(channel, req).addListener((future) -> {
                                if (future.isSuccess()) {
                                    if (this.isCors) {
                                        pipeline.remove(CorsHandler.class);
                                    }

                                    this.pojoEndpointServer.doOnOpen(channel, req, finalPattern);
                                } else {
                                    handshaker.close(channel, new CloseWebSocketFrame());
                                }

                            });
                        }

                    } else {
                        if (forbiddenByteBuf != null) {
                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, forbiddenByteBuf.retainedDuplicate());
                        } else {
                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
                        }

                        sendHttpResponse(ctx, req, res);
                    }
                }
            }
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        int statusCode = res.status().code();
        if (statusCode != HttpResponseStatus.OK.code() && res.content().readableBytes() == 0) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }

        HttpUtil.setContentLength(res, (long) res.content().readableBytes());
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || statusCode != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }

    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
        return "ws://" + location;
    }

    static {
        if (notFoundByteBuf == null) {
            notFoundByteBuf = buildStaticRes("/public/error/4xx.html");
        }

        if (badRequestByteBuf == null) {
            badRequestByteBuf = buildStaticRes("/public/error/4xx.html");
        }

        if (forbiddenByteBuf == null) {
            forbiddenByteBuf = buildStaticRes("/public/error/4xx.html");
        }

        if (internalServerErrorByteBuf == null) {
            internalServerErrorByteBuf = buildStaticRes("/public/error/5xx.html");
        }

    }
}
