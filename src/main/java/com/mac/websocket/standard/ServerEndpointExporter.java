package com.mac.websocket.standard;

import com.mac.websocket.annotation.EnableWebSocket;
import com.mac.websocket.annotation.ServerEndpoint;
import com.mac.websocket.pojo.PojoEndpointServer;
import com.mac.websocket.pojo.PojoMethodMapping;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.net.ssl.SSLException;
import javax.websocket.DeploymentException;
import java.net.InetSocketAddress;
import java.util.*;

public class ServerEndpointExporter extends ApplicationObjectSupport implements SmartInitializingSingleton, BeanFactoryAware, ResourceLoaderAware {
    @Autowired
    Environment environment;
    private AbstractBeanFactory beanFactory;
    private ResourceLoader resourceLoader;
    private final Map<InetSocketAddress, WebsocketServer> addressWebsocketServerMap = new HashMap();

    public ServerEndpointExporter() {
    }

    public void afterSingletonsInstantiated() {
        this.registerEndpoints();
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof AbstractBeanFactory)) {
            throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a AbstractBeanFactory: " + beanFactory);
        } else {
            this.beanFactory = (AbstractBeanFactory)beanFactory;
        }
    }

    protected void registerEndpoints() {
        ApplicationContext context = this.getApplicationContext();
        Assert.notNull(context, "ApplicationContext must not be null");
        this.scanPackage(context);
        String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
        Set<Class<?>> endpointClasses = new LinkedHashSet();
        String[] var4 = endpointBeanNames;
        int var5 = endpointBeanNames.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String beanName = var4[var6];
            endpointClasses.add(context.getType(beanName));
        }

        Iterator var8 = endpointClasses.iterator();

        while(var8.hasNext()) {
            Class<?> endpointClass = (Class)var8.next();
            if (AopUtils.isCglibProxy(endpointClass)) {
                this.registerEndpoint(endpointClass.getSuperclass());
            } else {
                this.registerEndpoint(endpointClass);
            }
        }

        this.init();
    }

    private void scanPackage(ApplicationContext context) {
        String[] basePackages = null;
        String[] enableWebSocketBeanNames = context.getBeanNamesForAnnotation(EnableWebSocket.class);
        String[] springBootApplicationBeanName = enableWebSocketBeanNames;
        int var5 = enableWebSocketBeanNames.length;

        int var6;
        String packageName;
        for(var6 = 0; var6 < var5; ++var6) {
            packageName = springBootApplicationBeanName[var6];
            Object enableWebSocketBean = context.getBean(packageName);
            EnableWebSocket enableWebSocket = (EnableWebSocket) AnnotationUtils.findAnnotation(enableWebSocketBean.getClass(), EnableWebSocket.class);

            assert enableWebSocket != null;

            if (enableWebSocket.scanBasePackages().length != 0) {
                basePackages = enableWebSocket.scanBasePackages();
                break;
            }
        }

        if (basePackages == null) {
            springBootApplicationBeanName = context.getBeanNamesForAnnotation(SpringBootApplication.class);
            Object springBootApplicationBean = context.getBean(springBootApplicationBeanName[0]);
            SpringBootApplication springBootApplication = (SpringBootApplication)AnnotationUtils.findAnnotation(springBootApplicationBean.getClass(), SpringBootApplication.class);

            assert springBootApplication != null;

            if (springBootApplication.scanBasePackages().length != 0) {
                basePackages = springBootApplication.scanBasePackages();
            } else {
                packageName = ClassUtils.getPackageName(springBootApplicationBean.getClass().getName());
                basePackages = new String[]{packageName};
            }
        }

        EndpointClassPathScanner scanHandle = new EndpointClassPathScanner((BeanDefinitionRegistry)context, false);
        if (this.resourceLoader != null) {
            scanHandle.setResourceLoader(this.resourceLoader);
        }

        String[] var12 = basePackages;
        var6 = basePackages.length;

        for(int var14 = 0; var14 < var6; ++var14) {
            String basePackage = var12[var14];
            scanHandle.doScan(new String[]{basePackage});
        }

    }

    private void init() {
        Iterator var1 = this.addressWebsocketServerMap.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<InetSocketAddress, WebsocketServer> entry = (Map.Entry)var1.next();
            WebsocketServer websocketServer = (WebsocketServer)entry.getValue();

            try {
                websocketServer.init();
                PojoEndpointServer pojoEndpointServer = websocketServer.getPojoEndpointServer();
                StringJoiner stringJoiner = new StringJoiner(",");
                pojoEndpointServer.getPathMatcherSet().forEach((pathMatcher) -> {
                    stringJoiner.add("'" + pathMatcher.getPattern() + "'");
                });
                this.logger.info(String.format("\u001b[34mNetty WebSocket started on port: %s with context path(s): %s .\u001b[0m", pojoEndpointServer.getPort(), stringJoiner));
            } catch (InterruptedException var6) {
                this.logger.error(String.format("websocket [%s] init fail", entry.getKey()), var6);
            } catch (SSLException var7) {
                this.logger.error(String.format("websocket [%s] ssl create fail", entry.getKey()), var7);
            }
        }

    }

    private void registerEndpoint(Class<?> endpointClass) {
        ServerEndpoint annotation = (ServerEndpoint) AnnotatedElementUtils.findMergedAnnotation(endpointClass, ServerEndpoint.class);
        if (annotation == null) {
            throw new IllegalStateException("missingAnnotation ServerEndpoint");
        } else {
            ServerEndpointConfig serverEndpointConfig = this.buildConfig(annotation);
            ApplicationContext context = this.getApplicationContext();

            PojoMethodMapping pojoMethodMapping;
            try {
                pojoMethodMapping = new PojoMethodMapping(endpointClass, context, this.beanFactory);
            } catch (DeploymentException var10) {
                throw new IllegalStateException("Failed to register ServerEndpointConfig: " + serverEndpointConfig, var10);
            }

            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverEndpointConfig.getHost(), serverEndpointConfig.getPort());
            String path = (String)this.resolveAnnotationValue(annotation.value(), String.class, "path");
            WebsocketServer websocketServer = (WebsocketServer)this.addressWebsocketServerMap.get(inetSocketAddress);
            if (websocketServer == null) {
                PojoEndpointServer pojoEndpointServer = new PojoEndpointServer(pojoMethodMapping, serverEndpointConfig, path);
                websocketServer = new WebsocketServer(pojoEndpointServer, serverEndpointConfig);
                this.addressWebsocketServerMap.put(inetSocketAddress, websocketServer);
            } else {
                websocketServer.getPojoEndpointServer().addPathPojoMethodMapping(path, pojoMethodMapping);
            }

        }
    }

    private ServerEndpointConfig buildConfig(ServerEndpoint annotation) {
        String host = (String)this.resolveAnnotationValue(annotation.host(), String.class, "host");
        int port = (Integer)this.resolveAnnotationValue(annotation.port(), Integer.class, "port");
        int bossLoopGroupThreads = (Integer)this.resolveAnnotationValue(annotation.bossLoopGroupThreads(), Integer.class, "bossLoopGroupThreads");
        int workerLoopGroupThreads = (Integer)this.resolveAnnotationValue(annotation.workerLoopGroupThreads(), Integer.class, "workerLoopGroupThreads");
        boolean useCompressionHandler = (Boolean)this.resolveAnnotationValue(annotation.useCompressionHandler(), Boolean.class, "useCompressionHandler");
        int optionConnectTimeoutMillis = (Integer)this.resolveAnnotationValue(annotation.optionConnectTimeoutMillis(), Integer.class, "optionConnectTimeoutMillis");
        int optionSoBacklog = (Integer)this.resolveAnnotationValue(annotation.optionSoBacklog(), Integer.class, "optionSoBacklog");
        int childOptionWriteSpinCount = (Integer)this.resolveAnnotationValue(annotation.childOptionWriteSpinCount(), Integer.class, "childOptionWriteSpinCount");
        int childOptionWriteBufferHighWaterMark = (Integer)this.resolveAnnotationValue(annotation.childOptionWriteBufferHighWaterMark(), Integer.class, "childOptionWriteBufferHighWaterMark");
        int childOptionWriteBufferLowWaterMark = (Integer)this.resolveAnnotationValue(annotation.childOptionWriteBufferLowWaterMark(), Integer.class, "childOptionWriteBufferLowWaterMark");
        int childOptionSoRcvbuf = (Integer)this.resolveAnnotationValue(annotation.childOptionSoRcvbuf(), Integer.class, "childOptionSoRcvbuf");
        int childOptionSoSndbuf = (Integer)this.resolveAnnotationValue(annotation.childOptionSoSndbuf(), Integer.class, "childOptionSoSndbuf");
        boolean childOptionTcpNodelay = (Boolean)this.resolveAnnotationValue(annotation.childOptionTcpNodelay(), Boolean.class, "childOptionTcpNodelay");
        boolean childOptionSoKeepalive = (Boolean)this.resolveAnnotationValue(annotation.childOptionSoKeepalive(), Boolean.class, "childOptionSoKeepalive");
        int childOptionSoLinger = (Integer)this.resolveAnnotationValue(annotation.childOptionSoLinger(), Integer.class, "childOptionSoLinger");
        boolean childOptionAllowHalfClosure = (Boolean)this.resolveAnnotationValue(annotation.childOptionAllowHalfClosure(), Boolean.class, "childOptionAllowHalfClosure");
        int readerIdleTimeSeconds = (Integer)this.resolveAnnotationValue(annotation.readerIdleTimeSeconds(), Integer.class, "readerIdleTimeSeconds");
        int writerIdleTimeSeconds = (Integer)this.resolveAnnotationValue(annotation.writerIdleTimeSeconds(), Integer.class, "writerIdleTimeSeconds");
        int allIdleTimeSeconds = (Integer)this.resolveAnnotationValue(annotation.allIdleTimeSeconds(), Integer.class, "allIdleTimeSeconds");
        int maxFramePayloadLength = (Integer)this.resolveAnnotationValue(annotation.maxFramePayloadLength(), Integer.class, "maxFramePayloadLength");
        boolean useEventExecutorGroup = (Boolean)this.resolveAnnotationValue(annotation.useEventExecutorGroup(), Boolean.class, "useEventExecutorGroup");
        int eventExecutorGroupThreads = (Integer)this.resolveAnnotationValue(annotation.eventExecutorGroupThreads(), Integer.class, "eventExecutorGroupThreads");
        String sslKeyPassword = (String)this.resolveAnnotationValue(annotation.sslKeyPassword(), String.class, "sslKeyPassword");
        String sslKeyStore = (String)this.resolveAnnotationValue(annotation.sslKeyStore(), String.class, "sslKeyStore");
        String sslKeyStorePassword = (String)this.resolveAnnotationValue(annotation.sslKeyStorePassword(), String.class, "sslKeyStorePassword");
        String sslKeyStoreType = (String)this.resolveAnnotationValue(annotation.sslKeyStoreType(), String.class, "sslKeyStoreType");
        String sslTrustStore = (String)this.resolveAnnotationValue(annotation.sslTrustStore(), String.class, "sslTrustStore");
        String sslTrustStorePassword = (String)this.resolveAnnotationValue(annotation.sslTrustStorePassword(), String.class, "sslTrustStorePassword");
        String sslTrustStoreType = (String)this.resolveAnnotationValue(annotation.sslTrustStoreType(), String.class, "sslTrustStoreType");
        String[] corsOrigins = annotation.corsOrigins();
        if (corsOrigins.length != 0) {
            for(int i = 0; i < corsOrigins.length; ++i) {
                corsOrigins[i] = (String)this.resolveAnnotationValue(corsOrigins[i], String.class, "corsOrigins");
            }
        }

        Boolean corsAllowCredentials = (Boolean)this.resolveAnnotationValue(annotation.corsAllowCredentials(), Boolean.class, "corsAllowCredentials");
        return new ServerEndpointConfig(host, port, bossLoopGroupThreads, workerLoopGroupThreads, useCompressionHandler, optionConnectTimeoutMillis, optionSoBacklog, childOptionWriteSpinCount, childOptionWriteBufferHighWaterMark, childOptionWriteBufferLowWaterMark, childOptionSoRcvbuf, childOptionSoSndbuf, childOptionTcpNodelay, childOptionSoKeepalive, childOptionSoLinger, childOptionAllowHalfClosure, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, maxFramePayloadLength, useEventExecutorGroup, eventExecutorGroupThreads, sslKeyPassword, sslKeyStore, sslKeyStorePassword, sslKeyStoreType, sslTrustStore, sslTrustStorePassword, sslTrustStoreType, corsOrigins, corsAllowCredentials);
    }

    private <T> T resolveAnnotationValue(Object value, Class<T> requiredType, String paramName) {
        if (value == null) {
            return null;
        } else {
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            if (value instanceof String) {
                String strVal = this.beanFactory.resolveEmbeddedValue((String)value);
                BeanExpressionResolver beanExpressionResolver = this.beanFactory.getBeanExpressionResolver();
                if (beanExpressionResolver != null) {
                    value = beanExpressionResolver.evaluate(strVal, new BeanExpressionContext(this.beanFactory, (Scope)null));
                } else {
                    value = strVal;
                }
            }

            try {
                return typeConverter.convertIfNecessary(value, requiredType);
            } catch (TypeMismatchException var7) {
                throw new IllegalArgumentException("Failed to convert value of parameter '" + paramName + "' to required type '" + requiredType.getName() + "'");
            }
        }
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
