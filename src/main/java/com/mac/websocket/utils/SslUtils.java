package com.mac.websocket.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.net.URL;
import java.security.KeyStore;

public final class SslUtils {
    public SslUtils() {
    }

    public static SslContext createSslContext(String keyPassword, String keyStoreResource, String keyStoreType, String keyStorePassword, String trustStoreResource, String trustStoreType, String trustStorePassword) throws SSLException {
        SslContextBuilder sslBuilder = SslContextBuilder.forServer(getKeyManagerFactory(keyStoreType, keyStoreResource, keyPassword, keyStorePassword)).trustManager(getTrustManagerFactory(trustStoreType, trustStoreResource, trustStorePassword));
        return sslBuilder.build();
    }

    private static KeyManagerFactory getKeyManagerFactory(String type, String resource, String keyPassword, String keyStorePassword) {
        try {
            KeyStore keyStore = loadKeyStore(type, resource, keyStorePassword);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            char[] keyPasswordBytes = !StringUtils.isEmpty(keyPassword) ? keyPassword.toCharArray() : null;
            if (keyPasswordBytes == null && !StringUtils.isEmpty(keyStorePassword)) {
                keyPasswordBytes = keyStorePassword.toCharArray();
            }

            keyManagerFactory.init(keyStore, keyPasswordBytes);
            return keyManagerFactory;
        } catch (Exception var7) {
            throw new IllegalStateException(var7);
        }
    }

    private static TrustManagerFactory getTrustManagerFactory(String trustStoreType, String trustStoreResource, String trustStorePassword) {
        try {
            KeyStore store = loadKeyStore(trustStoreType, trustStoreResource, trustStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(store);
            return trustManagerFactory;
        } catch (Exception var5) {
            throw new IllegalStateException(var5);
        }
    }

    private static KeyStore loadKeyStore(String type, String resource, String password) throws Exception {
        type = StringUtils.isEmpty(type) ? "JKS" : type;
        if (StringUtils.isEmpty(resource)) {
            return null;
        } else {
            KeyStore store = KeyStore.getInstance(type);
            URL url = ResourceUtils.getURL(resource);
            store.load(url.openStream(), StringUtils.isEmpty(password) ? null : password.toCharArray());
            return store;
        }
    }
}
