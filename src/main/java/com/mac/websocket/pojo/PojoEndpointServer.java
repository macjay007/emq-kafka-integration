package com.mac.websocket.pojo;

import com.mac.websocket.standard.ServerEndpointConfig;
import com.mac.websocket.support.AntPathMatcherWrapper;
import com.mac.websocket.support.DefaultPathMatcher;
import com.mac.websocket.support.MethodArgumentResolver;
import com.mac.websocket.support.WsPathMatcher;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMapMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;

import java.lang.reflect.Method;
import java.util.*;

public class PojoEndpointServer {
    private static final AttributeKey<Object> POJO_KEY = AttributeKey.valueOf("WEBSOCKET_IMPLEMENT");
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("WEBSOCKET_SESSION");
    private static final AttributeKey<String> PATH_KEY = AttributeKey.valueOf("WEBSOCKET_PATH");
    public static final AttributeKey<Map<String, String>> URI_TEMPLATE = AttributeKey.valueOf("WEBSOCKET_URI_TEMPLATE");
    public static final AttributeKey<Map<String, List<String>>> REQUEST_PARAM = AttributeKey.valueOf("WEBSOCKET_REQUEST_PARAM");
    private final Map<String, PojoMethodMapping> pathMethodMappingMap = new HashMap();
    private final ServerEndpointConfig config;
    private final Set<WsPathMatcher> pathMatchers = new HashSet();
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(PojoEndpointServer.class);

    public PojoEndpointServer(PojoMethodMapping methodMapping, ServerEndpointConfig config, String path) {
        this.addPathPojoMethodMapping(path, methodMapping);
        this.config = config;
    }

    public boolean hasBeforeHandshake(Channel channel, String path) {
        PojoMethodMapping methodMapping = this.getPojoMethodMapping(path, channel);
        return methodMapping.getBeforeHandshake() != null;
    }

    public void doBeforeHandshake(Channel channel, FullHttpRequest req, String path) {
        PojoMethodMapping methodMapping = null;
        methodMapping = this.getPojoMethodMapping(path, channel);
        Object implement = null;

        try {
            implement = methodMapping.getEndpointInstance();
        } catch (Exception var11) {
            LOGGER.error(var11);
            return;
        }

        channel.attr(POJO_KEY).set(implement);
        Session session = new Session(channel);
        channel.attr(SESSION_KEY).set(session);
        Method beforeHandshake = methodMapping.getBeforeHandshake();
        if (beforeHandshake != null) {
            try {
                beforeHandshake.invoke(implement, methodMapping.getBeforeHandshakeArgs(channel, req));
            } catch (TypeMismatchException var9) {
                throw var9;
            } catch (Throwable var10) {
                LOGGER.error(var10);
            }
        }

    }

    public void doOnOpen(Channel channel, FullHttpRequest req, String path) {
        PojoMethodMapping methodMapping = this.getPojoMethodMapping(path, channel);
        Object implement = channel.attr(POJO_KEY).get();
        if (implement == null) {
            try {
                implement = methodMapping.getEndpointInstance();
                channel.attr(POJO_KEY).set(implement);
            } catch (Exception var10) {
                LOGGER.error(var10);
                return;
            }

            Session session = new Session(channel);
            channel.attr(SESSION_KEY).set(session);
        }

        Method onOpenMethod = methodMapping.getOnOpen();
        if (onOpenMethod != null) {
            try {
                onOpenMethod.invoke(implement, methodMapping.getOnOpenArgs(channel, req));
            } catch (TypeMismatchException var8) {
                throw var8;
            } catch (Throwable var9) {
                LOGGER.error(var9);
            }
        }

    }

    public void doOnClose(Channel channel) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            String path = (String)attrPath.get();
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                return;
            }
        }

        if (methodMapping.getOnClose() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }

            Object implement = channel.attr(POJO_KEY).get();

            try {
                methodMapping.getOnClose().invoke(implement, methodMapping.getOnCloseArgs(channel));
            } catch (Throwable var6) {
                LOGGER.error(var6);
            }
        }

    }

    public void doOnError(Channel channel, Throwable throwable) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            String path = (String)attrPath.get();
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
        }

        if (methodMapping.getOnError() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }

            Object implement = channel.attr(POJO_KEY).get();

            try {
                Method method = methodMapping.getOnError();
                Object[] args = methodMapping.getOnErrorArgs(channel, throwable);
                method.invoke(implement, args);
            } catch (Throwable var8) {
                LOGGER.error(var8);
            }
        }

    }

    public void doOnMessage(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            String path = (String)attrPath.get();
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
        }

        if (methodMapping.getOnMessage() != null) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
            Object implement = channel.attr(POJO_KEY).get();

            try {
                methodMapping.getOnMessage().invoke(implement, methodMapping.getOnMessageArgs(channel, textFrame));
            } catch (Throwable var8) {
                LOGGER.error(var8);
            }
        }

    }

    public void doOnBinary(Channel channel, WebSocketFrame frame) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            String path = (String)attrPath.get();
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
        }

        if (methodMapping.getOnBinary() != null) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            Object implement = channel.attr(POJO_KEY).get();

            try {
                methodMapping.getOnBinary().invoke(implement, methodMapping.getOnBinaryArgs(channel, binaryWebSocketFrame));
            } catch (Throwable var8) {
                LOGGER.error(var8);
            }
        }

    }

    public void doOnEvent(Channel channel, Object evt) {
        Attribute<String> attrPath = channel.attr(PATH_KEY);
        PojoMethodMapping methodMapping = null;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            String path = (String)attrPath.get();
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
        }

        if (methodMapping.getOnEvent() != null) {
            if (!channel.hasAttr(SESSION_KEY)) {
                return;
            }

            Object implement = channel.attr(POJO_KEY).get();

            try {
                methodMapping.getOnEvent().invoke(implement, methodMapping.getOnEventArgs(channel, evt));
            } catch (Throwable var7) {
                LOGGER.error(var7);
            }
        }

    }

    public String getHost() {
        return this.config.getHost();
    }

    public int getPort() {
        return this.config.getPort();
    }

    public Set<WsPathMatcher> getPathMatcherSet() {
        return this.pathMatchers;
    }

    public void addPathPojoMethodMapping(String path, PojoMethodMapping pojoMethodMapping) {
        this.pathMethodMappingMap.put(path, pojoMethodMapping);
        MethodArgumentResolver[] var3 = pojoMethodMapping.getOnOpenArgResolvers();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            MethodArgumentResolver onOpenArgResolver = var3[var5];
            if (onOpenArgResolver instanceof PathVariableMethodArgumentResolver || onOpenArgResolver instanceof PathVariableMapMethodArgumentResolver) {
                this.pathMatchers.add(new AntPathMatcherWrapper(path));
                return;
            }
        }

        this.pathMatchers.add(new DefaultPathMatcher(path));
    }

    private PojoMethodMapping getPojoMethodMapping(String path, Channel channel) {
        PojoMethodMapping methodMapping;
        if (this.pathMethodMappingMap.size() == 1) {
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.values().iterator().next();
        } else {
            Attribute<String> attrPath = channel.attr(PATH_KEY);
            attrPath.set(path);
            methodMapping = (PojoMethodMapping)this.pathMethodMappingMap.get(path);
            if (methodMapping == null) {
                throw new RuntimeException("path " + path + " is not in pathMethodMappingMap ");
            }
        }

        return methodMapping;
    }
}

