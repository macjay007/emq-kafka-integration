package com.mac.websocket.standard;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ServerEndpointConfig {
        private final String HOST;
        private final int PORT;
        private final int BOSS_LOOP_GROUP_THREADS;
        private final int WORKER_LOOP_GROUP_THREADS;
        private final boolean USE_COMPRESSION_HANDLER;
        private final int CONNECT_TIMEOUT_MILLIS;
        private final int SO_BACKLOG;
        private final int WRITE_SPIN_COUNT;
        private final int WRITE_BUFFER_HIGH_WATER_MARK;
        private final int WRITE_BUFFER_LOW_WATER_MARK;
        private final int SO_RCVBUF;
        private final int SO_SNDBUF;
        private final boolean TCP_NODELAY;
        private final boolean SO_KEEPALIVE;
        private final int SO_LINGER;
        private final boolean ALLOW_HALF_CLOSURE;
        private final int READER_IDLE_TIME_SECONDS;
        private final int WRITER_IDLE_TIME_SECONDS;
        private final int ALL_IDLE_TIME_SECONDS;
        private final int MAX_FRAME_PAYLOAD_LENGTH;
        private final boolean USE_EVENT_EXECUTOR_GROUP;
        private final int EVENT_EXECUTOR_GROUP_THREADS;
        private final String KEY_PASSWORD;
        private final String KEY_STORE;
        private final String KEY_STORE_PASSWORD;
        private final String KEY_STORE_TYPE;
        private final String TRUST_STORE;
        private final String TRUST_STORE_PASSWORD;
        private final String TRUST_STORE_TYPE;
        private final String[] CORS_ORIGINS;
        private final Boolean CORS_ALLOW_CREDENTIALS;
        private static Integer randomPort;

        public ServerEndpointConfig(String host, int port, int bossLoopGroupThreads, int workerLoopGroupThreads, boolean useCompressionHandler, int connectTimeoutMillis, int soBacklog, int writeSpinCount, int writeBufferHighWaterMark, int writeBufferLowWaterMark, int soRcvbuf, int soSndbuf, boolean tcpNodelay, boolean soKeepalive, int soLinger, boolean allowHalfClosure, int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, int maxFramePayloadLength, boolean useEventExecutorGroup, int eventExecutorGroupThreads, String keyPassword, String keyStore, String keyStorePassword, String keyStoreType, String trustStore, String trustStorePassword, String trustStoreType, String[] corsOrigins, Boolean corsAllowCredentials) {
            if (!StringUtils.isEmpty(host) && !"0.0.0.0".equals(host) && !"0.0.0.0/0.0.0.0".equals(host)) {
                this.HOST = host;
            } else {
                this.HOST = "0.0.0.0";
            }

            this.PORT = this.getAvailablePort(port);
            this.BOSS_LOOP_GROUP_THREADS = bossLoopGroupThreads;
            this.WORKER_LOOP_GROUP_THREADS = workerLoopGroupThreads;
            this.USE_COMPRESSION_HANDLER = useCompressionHandler;
            this.CONNECT_TIMEOUT_MILLIS = connectTimeoutMillis;
            this.SO_BACKLOG = soBacklog;
            this.WRITE_SPIN_COUNT = writeSpinCount;
            this.WRITE_BUFFER_HIGH_WATER_MARK = writeBufferHighWaterMark;
            this.WRITE_BUFFER_LOW_WATER_MARK = writeBufferLowWaterMark;
            this.SO_RCVBUF = soRcvbuf;
            this.SO_SNDBUF = soSndbuf;
            this.TCP_NODELAY = tcpNodelay;
            this.SO_KEEPALIVE = soKeepalive;
            this.SO_LINGER = soLinger;
            this.ALLOW_HALF_CLOSURE = allowHalfClosure;
            this.READER_IDLE_TIME_SECONDS = readerIdleTimeSeconds;
            this.WRITER_IDLE_TIME_SECONDS = writerIdleTimeSeconds;
            this.ALL_IDLE_TIME_SECONDS = allIdleTimeSeconds;
            this.MAX_FRAME_PAYLOAD_LENGTH = maxFramePayloadLength;
            this.USE_EVENT_EXECUTOR_GROUP = useEventExecutorGroup;
            this.EVENT_EXECUTOR_GROUP_THREADS = eventExecutorGroupThreads;
            this.KEY_PASSWORD = keyPassword;
            this.KEY_STORE = keyStore;
            this.KEY_STORE_PASSWORD = keyStorePassword;
            this.KEY_STORE_TYPE = keyStoreType;
            this.TRUST_STORE = trustStore;
            this.TRUST_STORE_PASSWORD = trustStorePassword;
            this.TRUST_STORE_TYPE = trustStoreType;
            this.CORS_ORIGINS = corsOrigins;
            this.CORS_ALLOW_CREDENTIALS = corsAllowCredentials;
        }

        private int getAvailablePort(int port) {
            if (port != 0) {
                return port;
            } else if (randomPort != null && randomPort != 0) {
                return randomPort;
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
                Socket socket = new Socket();

                try {
                    socket.bind(inetSocketAddress);
                } catch (IOException var7) {
                    var7.printStackTrace();
                }

                int localPort = socket.getLocalPort();

                try {
                    socket.close();
                } catch (IOException var6) {
                    var6.printStackTrace();
                }

                randomPort = localPort;
                return localPort;
            }
        }

        public String getHost() {
            return this.HOST;
        }

        public int getPort() {
            return this.PORT;
        }

        public int getBossLoopGroupThreads() {
            return this.BOSS_LOOP_GROUP_THREADS;
        }

        public int getWorkerLoopGroupThreads() {
            return this.WORKER_LOOP_GROUP_THREADS;
        }

        public boolean isUseCompressionHandler() {
            return this.USE_COMPRESSION_HANDLER;
        }

        public int getConnectTimeoutMillis() {
            return this.CONNECT_TIMEOUT_MILLIS;
        }

        public int getSoBacklog() {
            return this.SO_BACKLOG;
        }

        public int getWriteSpinCount() {
            return this.WRITE_SPIN_COUNT;
        }

        public int getWriteBufferHighWaterMark() {
            return this.WRITE_BUFFER_HIGH_WATER_MARK;
        }

        public int getWriteBufferLowWaterMark() {
            return this.WRITE_BUFFER_LOW_WATER_MARK;
        }

        public int getSoRcvbuf() {
            return this.SO_RCVBUF;
        }

        public int getSoSndbuf() {
            return this.SO_SNDBUF;
        }

        public boolean isTcpNodelay() {
            return this.TCP_NODELAY;
        }

        public boolean isSoKeepalive() {
            return this.SO_KEEPALIVE;
        }

        public int getSoLinger() {
            return this.SO_LINGER;
        }

        public boolean isAllowHalfClosure() {
            return this.ALLOW_HALF_CLOSURE;
        }

        public static Integer getRandomPort() {
            return randomPort;
        }

        public int getReaderIdleTimeSeconds() {
            return this.READER_IDLE_TIME_SECONDS;
        }

        public int getWriterIdleTimeSeconds() {
            return this.WRITER_IDLE_TIME_SECONDS;
        }

        public int getAllIdleTimeSeconds() {
            return this.ALL_IDLE_TIME_SECONDS;
        }

        public int getmaxFramePayloadLength() {
            return this.MAX_FRAME_PAYLOAD_LENGTH;
        }

        public boolean isUseEventExecutorGroup() {
            return this.USE_EVENT_EXECUTOR_GROUP;
        }

        public int getEventExecutorGroupThreads() {
            return this.EVENT_EXECUTOR_GROUP_THREADS;
        }

        public String getKeyPassword() {
            return this.KEY_PASSWORD;
        }

        public String getKeyStore() {
            return this.KEY_STORE;
        }

        public String getKeyStorePassword() {
            return this.KEY_STORE_PASSWORD;
        }

        public String getKeyStoreType() {
            return this.KEY_STORE_TYPE;
        }

        public String getTrustStore() {
            return this.TRUST_STORE;
        }

        public String getTrustStorePassword() {
            return this.TRUST_STORE_PASSWORD;
        }

        public String getTrustStoreType() {
            return this.TRUST_STORE_TYPE;
        }

        public String[] getCorsOrigins() {
            return this.CORS_ORIGINS;
        }

        public Boolean getCorsAllowCredentials() {
            return this.CORS_ALLOW_CREDENTIALS;
        }
    }
