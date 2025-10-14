package com.github.jdussouillez.services;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.NoSuchElementException;
import java.util.UUID;

@Interceptor
@Priority(4000)
@RegisterGrpcServerErrorHandler
public class GrpcServerErrorHandler {

    private static final Metadata.Key<String> TRAILER_KEY_EXCEPTION = Metadata.Key.of(
        "x-exception",
        Metadata.ASCII_STRING_MARSHALLER
    );

    private static final Metadata.Key<String> TRAILER_KEY_TRACE_ID = Metadata.Key.of(
        "x-trace-id",
        Metadata.ASCII_STRING_MARSHALLER
    );

    @AroundInvoke
    public Object addOnFailureOnMutiny(final InvocationContext context) throws Exception {
        var obj = context.proceed();
        if (Uni.class.isAssignableFrom(obj.getClass())) {
            return ((Uni<?>) obj)
                .onFailure()
                .transform(this::map);
        }
        return obj;
    }

    private StatusRuntimeException map(final Throwable throwable) {
        return Status.fromCode(getCode(throwable))
            .withDescription(throwable.getMessage())
            .withCause(throwable.getCause())
            .asRuntimeException(getTrailers(throwable));
    }

    protected Status.Code getCode(final Throwable throwable) {
        if (throwable instanceof IllegalArgumentException) {
            return Status.Code.INVALID_ARGUMENT;
        }
        if (throwable instanceof NoSuchElementException) {
            return Status.Code.NOT_FOUND;
        }
        // ...
        return Status.Code.INTERNAL;
    }

    protected Metadata getTrailers(final Throwable throwable) {
        var trailers = new Metadata();
        trailers.put(TRAILER_KEY_EXCEPTION, throwable.getClass().getName());
        trailers.put(TRAILER_KEY_TRACE_ID, UUID.randomUUID().toString());
        // ...
        return trailers;
    }
}
