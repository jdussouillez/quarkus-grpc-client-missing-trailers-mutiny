package com.github.jdussouillez;

import com.github.jdussouillez.hello.HelloGrpcApiGrpc;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;

public class Server extends AbstractVerticle {

    @Override
    public void start() {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        grpcServer.callHandler(HelloGrpcApiGrpc.getSayHelloMethod(), req -> {
            req.handler(request -> {
                System.out.println("Sending response");
                req.response().trailers().add("grpc-message", "Invalid name");
                req.response().trailers().add("error-details", "foo bar baz");
                req.response()
                    .status(GrpcStatus.INVALID_ARGUMENT)
                    .end();
            });
        });

        vertx.createHttpServer()
            .requestHandler(grpcServer)
            .listen(50051, ar -> {
                if (ar.succeeded()) {
                    System.out.println("✅ gRPC Vert.x server running on port 50051");
                } else {
                    ar.cause().printStackTrace();
                }
            });
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Server());
    }
}
