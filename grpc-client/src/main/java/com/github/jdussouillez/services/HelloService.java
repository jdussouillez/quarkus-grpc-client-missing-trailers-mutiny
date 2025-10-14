package com.github.jdussouillez.services;

import com.github.jdussouillez.hello.HelloGrpcApi;
import com.github.jdussouillez.hello.HelloRequest;
import com.github.jdussouillez.hello.HelloResponse;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.time.Duration;

@ApplicationScoped
public class HelloService {

    @GrpcClient("hello")
    protected HelloGrpcApi helloGrpcApi;

    @RegisterGrpcClientErrorHandler
    protected Uni<Void> sayHello() {
        return helloGrpcApi.sayHello(HelloRequest.newBuilder().setName("error").build())
            .map(HelloResponse::getMessage)
            .replaceWithVoid();
    }

    void onStartup(@Observes final StartupEvent event) {
        sayHello()
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            .onFailure()
            .recoverWithNull()
            .await()
            .atMost(Duration.ofSeconds(5));
    }
}
