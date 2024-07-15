package com.mac.websocket.standard;

import com.mac.websocket.pojo.PojoEndpointServer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;


public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final PojoEndpointServer pojoEndpointServer;

    public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer) {
        this.pojoEndpointServer = pojoEndpointServer;
    }

    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        this.handleWebSocketFrame(ctx, msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.pojoEndpointServer.doOnClose(ctx.channel());
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.pojoEndpointServer.doOnEvent(ctx.channel(), evt);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            this.pojoEndpointServer.doOnMessage(ctx.channel(), frame);
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
        } else if (frame instanceof BinaryWebSocketFrame) {
            this.pojoEndpointServer.doOnBinary(ctx.channel(), frame);
        } else {
            if (frame instanceof PongWebSocketFrame) {
            }

        }
    }
}
