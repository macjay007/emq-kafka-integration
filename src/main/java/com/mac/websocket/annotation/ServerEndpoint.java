package com.mac.websocket.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ServerEndpoint {
    @AliasFor("path")
    String value() default "/";

    @AliasFor("value")
    String path() default "/";

    String host() default "0.0.0.0";

    String port() default "80";

    String bossLoopGroupThreads() default "1";

    String workerLoopGroupThreads() default "0";

    String useCompressionHandler() default "false";

    String optionConnectTimeoutMillis() default "30000";

    String optionSoBacklog() default "128";

    String childOptionWriteSpinCount() default "16";

    String childOptionWriteBufferHighWaterMark() default "65536";

    String childOptionWriteBufferLowWaterMark() default "32768";

    String childOptionSoRcvbuf() default "-1";

    String childOptionSoSndbuf() default "-1";

    String childOptionTcpNodelay() default "true";

    String childOptionSoKeepalive() default "false";

    String childOptionSoLinger() default "-1";

    String childOptionAllowHalfClosure() default "false";

    String readerIdleTimeSeconds() default "0";

    String writerIdleTimeSeconds() default "0";

    String allIdleTimeSeconds() default "0";

    String maxFramePayloadLength() default "65536";

    String useEventExecutorGroup() default "true";

    String eventExecutorGroupThreads() default "16";

    String sslKeyPassword() default "";

    String sslKeyStore() default "";

    String sslKeyStorePassword() default "";

    String sslKeyStoreType() default "";

    String sslTrustStore() default "";

    String sslTrustStorePassword() default "";

    String sslTrustStoreType() default "";

    String[] corsOrigins() default {};

    String corsAllowCredentials() default "";
}
