package com.mac.websocket.pojo;

import com.mac.websocket.annotation.*;
import com.mac.websocket.support.*;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;

import javax.websocket.DeploymentException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PojoMethodMapping {
    private static final ParameterNameDiscoverer DEFAULT_PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private final Method beforeHandshake;
    private final Method onOpen;
    private final Method onClose;
    private final Method onError;
    private final Method onMessage;
    private final Method onBinary;
    private final Method onEvent;
    private final MethodParameter[] beforeHandshakeParameters;
    private final MethodParameter[] onOpenParameters;
    private final MethodParameter[] onCloseParameters;
    private final MethodParameter[] onErrorParameters;
    private final MethodParameter[] onMessageParameters;
    private final MethodParameter[] onBinaryParameters;
    private final MethodParameter[] onEventParameters;
    private final MethodArgumentResolver[] beforeHandshakeArgResolvers;
    private final MethodArgumentResolver[] onOpenArgResolvers;
    private final MethodArgumentResolver[] onCloseArgResolvers;
    private final MethodArgumentResolver[] onErrorArgResolvers;
    private final MethodArgumentResolver[] onMessageArgResolvers;
    private final MethodArgumentResolver[] onBinaryArgResolvers;
    private final MethodArgumentResolver[] onEventArgResolvers;
    private final Class pojoClazz;
    private final ApplicationContext applicationContext;
    private final AbstractBeanFactory beanFactory;

    public PojoMethodMapping(Class<?> pojoClazz, ApplicationContext context, AbstractBeanFactory beanFactory) throws DeploymentException {
        this.applicationContext = context;
        this.pojoClazz = pojoClazz;
        this.beanFactory = beanFactory;
        Method handshake = null;
        Method open = null;
        Method close = null;
        Method error = null;
        Method message = null;
        Method binary = null;
        Method event = null;
        Method[] pojoClazzMethods = null;

        for (Class<?> currentClazz = pojoClazz; !currentClazz.equals(Object.class); currentClazz = currentClazz.getSuperclass()) {
            Method[] currentClazzMethods = currentClazz.getDeclaredMethods();
            if (currentClazz == pojoClazz) {
                pojoClazzMethods = currentClazzMethods;
            }

            Method[] var14 = currentClazzMethods;
            int var15 = currentClazzMethods.length;

            for (int var16 = 0; var16 < var15; ++var16) {
                Method method = var14[var16];
                if (method.getAnnotation(BeforeHandshake.class) != null) {
                    this.checkPublic(method);
                    if (handshake == null) {
                        handshake = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(handshake, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation BeforeHandshake");
                    }
                } else if (method.getAnnotation(OnOpen.class) != null) {
                    this.checkPublic(method);
                    if (open == null) {
                        open = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(open, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation OnOpen");
                    }
                } else if (method.getAnnotation(OnClose.class) != null) {
                    this.checkPublic(method);
                    if (close == null) {
                        close = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(close, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation OnClose");
                    }
                } else if (method.getAnnotation(OnError.class) != null) {
                    this.checkPublic(method);
                    if (error == null) {
                        error = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(error, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation OnError");
                    }
                } else if (method.getAnnotation(OnMessage.class) != null) {
                    this.checkPublic(method);
                    if (message == null) {
                        message = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(message, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation onMessage");
                    }
                } else if (method.getAnnotation(OnBinary.class) != null) {
                    this.checkPublic(method);
                    if (binary == null) {
                        binary = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(binary, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation OnBinary");
                    }
                } else if (method.getAnnotation(OnEvent.class) != null) {
                    this.checkPublic(method);
                    if (event == null) {
                        event = method;
                    } else if (currentClazz == pojoClazz || !this.isMethodOverride(event, method)) {
                        throw new DeploymentException("pojoMethodMapping.duplicateAnnotation OnEvent");
                    }
                }
            }
        }

        if (handshake != null && handshake.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, handshake, BeforeHandshake.class)) {
            handshake = null;
        }

        if (open != null && open.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, open, OnOpen.class)) {
            open = null;
        }

        if (close != null && close.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, close, OnClose.class)) {
            close = null;
        }

        if (error != null && error.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, error, OnError.class)) {
            error = null;
        }

        if (message != null && message.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, message, OnMessage.class)) {
            message = null;
        }

        if (binary != null && binary.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, binary, OnBinary.class)) {
            binary = null;
        }

        if (event != null && event.getDeclaringClass() != pojoClazz && this.isOverrideWithoutAnnotation(pojoClazzMethods, event, OnEvent.class)) {
            event = null;
        }

        this.beforeHandshake = handshake;
        this.onOpen = open;
        this.onClose = close;
        this.onError = error;
        this.onMessage = message;
        this.onBinary = binary;
        this.onEvent = event;
        this.beforeHandshakeParameters = getParameters(this.beforeHandshake);
        this.onOpenParameters = getParameters(this.onOpen);
        this.onCloseParameters = getParameters(this.onClose);
        this.onMessageParameters = getParameters(this.onMessage);
        this.onErrorParameters = getParameters(this.onError);
        this.onBinaryParameters = getParameters(this.onBinary);
        this.onEventParameters = getParameters(this.onEvent);
        this.beforeHandshakeArgResolvers = this.getResolvers(this.beforeHandshakeParameters);
        this.onOpenArgResolvers = this.getResolvers(this.onOpenParameters);
        this.onCloseArgResolvers = this.getResolvers(this.onCloseParameters);
        this.onMessageArgResolvers = this.getResolvers(this.onMessageParameters);
        this.onErrorArgResolvers = this.getResolvers(this.onErrorParameters);
        this.onBinaryArgResolvers = this.getResolvers(this.onBinaryParameters);
        this.onEventArgResolvers = this.getResolvers(this.onEventParameters);
    }

    private void checkPublic(Method m) throws DeploymentException {
        if (!Modifier.isPublic(m.getModifiers())) {
            throw new DeploymentException("pojoMethodMapping.methodNotPublic " + m.getName());
        }
    }

    private boolean isMethodOverride(Method method1, Method method2) {
        return method1.getName().equals(method2.getName()) && method1.getReturnType().equals(method2.getReturnType()) && Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes());
    }

    private boolean isOverrideWithoutAnnotation(Method[] methods, Method superclazzMethod, Class<? extends Annotation> annotation) {
        Method[] var4 = methods;
        int var5 = methods.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Method method = var4[var6];
            if (this.isMethodOverride(method, superclazzMethod) && method.getAnnotation(annotation) == null) {
                return true;
            }
        }

        return false;
    }

    Object getEndpointInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object implement = this.pojoClazz.getDeclaredConstructor().newInstance();
        AutowiredAnnotationBeanPostProcessor postProcessor = (AutowiredAnnotationBeanPostProcessor) this.applicationContext.getBean(AutowiredAnnotationBeanPostProcessor.class);
        postProcessor.postProcessProperties((PropertyValues) null, implement, (String) null);
        return implement;
    }

    Method getBeforeHandshake() {
        return this.beforeHandshake;
    }

    Object[] getBeforeHandshakeArgs(Channel channel, FullHttpRequest req) throws Exception {
        return this.getMethodArgumentValues(channel, req, this.beforeHandshakeParameters, this.beforeHandshakeArgResolvers);
    }

    Method getOnOpen() {
        return this.onOpen;
    }

    Object[] getOnOpenArgs(Channel channel, FullHttpRequest req) throws Exception {
        return this.getMethodArgumentValues(channel, req, this.onOpenParameters, this.onOpenArgResolvers);
    }

    MethodArgumentResolver[] getOnOpenArgResolvers() {
        return this.onOpenArgResolvers;
    }

    Method getOnClose() {
        return this.onClose;
    }

    Object[] getOnCloseArgs(Channel channel) throws Exception {
        return this.getMethodArgumentValues(channel, (Object) null, this.onCloseParameters, this.onCloseArgResolvers);
    }

    Method getOnError() {
        return this.onError;
    }

    Object[] getOnErrorArgs(Channel channel, Throwable throwable) throws Exception {
        return this.getMethodArgumentValues(channel, throwable, this.onErrorParameters, this.onErrorArgResolvers);
    }

    Method getOnMessage() {
        return this.onMessage;
    }

    Object[] getOnMessageArgs(Channel channel, TextWebSocketFrame textWebSocketFrame) throws Exception {
        return this.getMethodArgumentValues(channel, textWebSocketFrame, this.onMessageParameters, this.onMessageArgResolvers);
    }

    Method getOnBinary() {
        return this.onBinary;
    }

    Object[] getOnBinaryArgs(Channel channel, BinaryWebSocketFrame binaryWebSocketFrame) throws Exception {
        return this.getMethodArgumentValues(channel, binaryWebSocketFrame, this.onBinaryParameters, this.onBinaryArgResolvers);
    }

    Method getOnEvent() {
        return this.onEvent;
    }

    Object[] getOnEventArgs(Channel channel, Object evt) throws Exception {
        return this.getMethodArgumentValues(channel, evt, this.onEventParameters, this.onEventArgResolvers);
    }

    private Object[] getMethodArgumentValues(Channel channel, Object object, MethodParameter[] parameters, MethodArgumentResolver[] resolvers) throws Exception {
        Object[] objects = new Object[parameters.length];

        for (int i = 0; i < parameters.length; ++i) {
            MethodParameter parameter = parameters[i];
            MethodArgumentResolver resolver = resolvers[i];
            Object arg = resolver.resolveArgument(parameter, channel, object);
            objects[i] = arg;
        }

        return objects;
    }

    private MethodArgumentResolver[] getResolvers(MethodParameter[] parameters) throws DeploymentException {
        MethodArgumentResolver[] methodArgumentResolvers = new MethodArgumentResolver[parameters.length];
        List<MethodArgumentResolver> resolvers = this.getDefaultResolvers();

        for (int i = 0; i < parameters.length; ++i) {
            MethodParameter parameter = parameters[i];
            Iterator var6 = resolvers.iterator();

            while (var6.hasNext()) {
                MethodArgumentResolver resolver = (MethodArgumentResolver) var6.next();
                if (resolver.supportsParameter(parameter)) {
                    methodArgumentResolvers[i] = resolver;
                    break;
                }
            }

            if (methodArgumentResolvers[i] == null) {
                throw new DeploymentException("pojoMethodMapping.paramClassIncorrect parameter name : " + parameter.getParameterName());
            }
        }

        return methodArgumentResolvers;
    }

    private List<MethodArgumentResolver> getDefaultResolvers() {
        List<MethodArgumentResolver> resolvers = new ArrayList();
        resolvers.add(new SessionMethodArgumentResolver());
        resolvers.add(new HttpHeadersMethodArgumentResolver());
        resolvers.add(new TextMethodArgumentResolver());
        resolvers.add(new ThrowableMethodArgumentResolver());
        resolvers.add(new ByteMethodArgumentResolver());
        resolvers.add(new RequestParamMapMethodArgumentResolver());
        resolvers.add(new RequestParamMethodArgumentResolver(this.beanFactory));
        resolvers.add(new PathVariableMapMethodArgumentResolver());
        resolvers.add(new PathVariableMethodArgumentResolver(this.beanFactory));
        resolvers.add(new EventMethodArgumentResolver(this.beanFactory));
        return resolvers;
    }

    private static MethodParameter[] getParameters(Method m) {
        if (m == null) {
            return new MethodParameter[0];
        } else {
            int count = m.getParameterCount();
            MethodParameter[] result = new MethodParameter[count];

            for (int i = 0; i < count; ++i) {
                MethodParameter methodParameter = new MethodParameter(m, i);
                methodParameter.initParameterNameDiscovery(DEFAULT_PARAMETER_NAME_DISCOVERER);
                result[i] = methodParameter;
            }

            return result;
        }
    }
}
