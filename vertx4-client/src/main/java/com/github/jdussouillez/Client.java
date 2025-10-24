package com.github.jdussouillez;

import com.github.jdussouillez.hello.HelloRequest;
import com.github.jdussouillez.hello.HelloResponse;
import com.github.jdussouillez.hello.VertxHelloGrpcApiGrpcClient;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientRequest;
import io.vertx.grpc.client.GrpcClientResponse;

public class Client {

    public static void main(String[] args) {
        var vertx = Vertx.vertx();
        GrpcClient client = GrpcClient.client(vertx);
        SocketAddress server = SocketAddress.inetSocketAddress(50051, "localhost");
        var sayHelloMethod = VertxHelloGrpcApiGrpcClient.SayHello;
        client.request(server, sayHelloMethod)
            .onSuccess((GrpcClientRequest<HelloRequest, HelloResponse> req) -> {
                var helloReq = HelloRequest.newBuilder().setName("Test").build();
                req.end(helloReq);
                req.response()
                    .onSuccess((GrpcClientResponse<HelloRequest, HelloResponse> res) -> {
                        System.out.println(res.trailers());
                        res.handler(reply -> {
                            System.out.println("Server replied: " + reply.getMessage());
                        });
                    })
                    .onFailure(err -> {
                        err.printStackTrace();
                    })
                    .onComplete(r -> {
                        System.out.println("Response completed. Success: " + r.succeeded());
                        vertx.close();
                    });
            })
            .onFailure(Throwable::printStackTrace);
    }
}
