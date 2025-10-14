package com.github.jdussouillez.services;

import com.github.jdussouillez.hello.HelloGrpcApi;
import com.github.jdussouillez.hello.HelloRequest;
import com.github.jdussouillez.hello.HelloResponse;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class HelloGrpcApiService implements HelloGrpcApi {

    @Override
    @RegisterGrpcServerErrorHandler
    public Uni<HelloResponse> sayHello(final HelloRequest req) {
        return sayHello(req.getName())
            .map(msg -> HelloResponse.newBuilder().setMessage(msg).build());
    }

    private Uni<String> sayHello(final String name) {
        if (name.toLowerCase().startsWith("error")) {
            return Uni.createFrom().failure(new IllegalArgumentException("Name '%s' is invalid".formatted(name)));
        }
        return Uni.createFrom().item("Hello %s!".formatted(name));
    }
}
