package com.mac.exhook.server;


import com.google.protobuf.ByteString;
import com.mac.exhook.grpcjava.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * emqx的钩子服务
 * @author mac
 */
public class ExServer {
    private static final Logger logger = Logger.getLogger(ExServer.class.getName());

    private Server server;

    public void start() throws IOException {
        /* 服务端口 */
        int port = 9000;

        server = ServerBuilder.forPort(port)
                .addService(new HookProviderImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    ExServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ExServer server = new ExServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class HookProviderImpl extends HookProviderGrpc.HookProviderImplBase {

        public void DEBUG(String fn, Object req) {
            System.out.println("事件：" + fn + ", request: " + req+"------结束");
        }

        /**
         * 注册钩子加载,开启钩子服务,onProviderLoaded中目前包含所有的钩子服务，可以将用的放开注释，只需要在本类中实现需要的方法即可
         * @param request
         * @param responseObserver
         */
        @Override
        public void onProviderLoaded(ProviderLoadedRequest request, StreamObserver<LoadedResponse> responseObserver) {
            DEBUG("onProviderLoaded", request);
            HookSpec[] specs = {
                    HookSpec.newBuilder().setName("client.connect").build(),
                    HookSpec.newBuilder().setName("client.connack").build(),
                    HookSpec.newBuilder().setName("client.connected").build(),
//                    HookSpec.newBuilder().setName("client.disconnected").build(),
//                    HookSpec.newBuilder().setName("client.authenticate").build(),
//                    HookSpec.newBuilder().setName("client.authorize").build(),
//                    HookSpec.newBuilder().setName("client.subscribe").build(),
//                    HookSpec.newBuilder().setName("client.unsubscribe").build(),
//
//                    HookSpec.newBuilder().setName("session.created").build(),
//                    HookSpec.newBuilder().setName("session.subscribed").build(),
//                    HookSpec.newBuilder().setName("session.unsubscribed").build(),
//                    HookSpec.newBuilder().setName("session.resumed").build(),
//                    HookSpec.newBuilder().setName("session.discarded").build(),
//                    HookSpec.newBuilder().setName("session.takenover").build(),
//                    HookSpec.newBuilder().setName("session.terminated").build(),

//                    HookSpec.newBuilder().setName("message.publish").build(),
//                    HookSpec.newBuilder().setName("message.delivered").build(),
//                    HookSpec.newBuilder().setName("message.acked").build(),
//                    HookSpec.newBuilder().setName("message.dropped").build()
            };
            LoadedResponse reply = LoadedResponse.newBuilder().addAllHooks(Arrays.asList(specs)).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * 关闭钩子服务
         * @param request
         * @param responseObserver
         */
        @Override
        public void onProviderUnloaded(ProviderUnloadedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onProviderUnloaded", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * 客户端连接
         * @param request
         * @param responseObserver
         */
        @Override
        public void onClientConnect(ClientConnectRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnect", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientConnack(ClientConnackRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnack", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * 客户端连接成功
         * @param request
         * @param responseObserver
         */
        @Override
        public void onClientConnected(ClientConnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientConnected", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * 客户端断开连接
         * @param request
         * @param responseObserver
         */
        @Override
        public void onClientDisconnected(ClientDisconnectedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientDisconnected", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        /**
         * 认证，单独开启认证功能后，该钩子服务失效
         * @param request
         * @param responseObserver
         */
        @Override
        public void onClientAuthenticate(ClientAuthenticateRequest request, StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onClientAuthenticate", request);
            ValuedResponse reply = ValuedResponse.newBuilder()
                    .setBoolResult(true)
                    .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientAuthorize(ClientAuthorizeRequest request, StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onClientAuthorize", request);
            super.onClientAuthorize(request, responseObserver);
        }


        @Override
        public void onClientSubscribe(ClientSubscribeRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientSubscribe", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onClientUnsubscribe(ClientUnsubscribeRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onClientUnsubscribe", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionCreated(SessionCreatedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionCreated", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionSubscribed(SessionSubscribedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionSubscribed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionUnsubscribed(SessionUnsubscribedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionUnsubscribed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionResumed(SessionResumedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionResumed", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionDiscarded(SessionDiscardedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionDdiscarded", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onSessionTakenover(SessionTakenoverRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionTakenover", request);
            super.onSessionTakenover(request, responseObserver);
        }

        @Override
        public void onSessionTerminated(SessionTerminatedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onSessionTerminated", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessagePublish(MessagePublishRequest request, StreamObserver<ValuedResponse> responseObserver) {
            DEBUG("onMessagePublish", request);

            ByteString bstr = ByteString.copyFromUtf8("hardcode payload by exhook-svr-java :)");

            Message nmsg = Message.newBuilder()
                    .setId(request.getMessage().getId())
                    .setNode(request.getMessage().getNode())
                    .setFrom(request.getMessage().getFrom())
                    .setTopic(request.getMessage().getTopic())
                    .setPayload(bstr).build();


            ValuedResponse reply = ValuedResponse.newBuilder()
                    .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
                    .setMessage(nmsg).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

// case2: stop publish the 't/d' messages
//        @Override
//        public void onMessagePublish(MessagePublishRequest request, StreamObserver<ValuedResponse> responseObserver) {
//            DEBUG("onMessagePublish", request);
//
//            Message nmsg = request.getMessage();
//            if ("t/d".equals(nmsg.getTopic())) {
//                ByteString bstr = ByteString.copyFromUtf8("");
//                nmsg = Message.newBuilder()
//                              .setId     (request.getMessage().getId())
//                              .setNode   (request.getMessage().getNode())
//                              .setFrom   (request.getMessage().getFrom())
//                              .setTopic  (request.getMessage().getTopic())
//                              .setPayload(bstr)
//                              .putHeaders("allow_publish", "false").build();
//            }
//
//            ValuedResponse reply = ValuedResponse.newBuilder()
//                                                 .setType(ValuedResponse.ResponsedType.STOP_AND_RETURN)
//                                                 .setMessage(nmsg).build();
//            responseObserver.onNext(reply);
//            responseObserver.onCompleted();
//        }

        @Override
        public void onMessageDelivered(MessageDeliveredRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageDelivered", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessageAcked(MessageAckedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageAcked", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void onMessageDropped(MessageDroppedRequest request, StreamObserver<EmptySuccess> responseObserver) {
            DEBUG("onMessageDropped", request);
            EmptySuccess reply = EmptySuccess.newBuilder().build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
