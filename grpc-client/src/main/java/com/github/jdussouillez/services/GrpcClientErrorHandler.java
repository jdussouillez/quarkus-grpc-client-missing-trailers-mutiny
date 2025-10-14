package com.github.jdussouillez.services;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Interceptor
@Priority(4000)
@RegisterGrpcClientErrorHandler
public class GrpcClientErrorHandler {

    private static final Metadata.Key<String> TRAILER_KEY_EXCEPTION = Metadata.Key.of(
        "x-exception",
        Metadata.ASCII_STRING_MARSHALLER
    );

    private static final Metadata.Key<String> TRAILER_KEY_TRACE_ID = Metadata.Key.of(
        "x-trace-id",
        Metadata.ASCII_STRING_MARSHALLER
    );

    private static final Set<Metadata.Key<String>> KEYS = Set.of(TRAILER_KEY_EXCEPTION, TRAILER_KEY_TRACE_ID);

    @AroundInvoke
    public Object addOnFailureOnMutiny(final InvocationContext context) throws Exception {
        var obj = context.proceed();
        if (Uni.class.isAssignableFrom(obj.getClass())) {
            return ((Uni<?>) obj).onFailure().invoke(this::logGrpcFailure);
        }
        return obj;
    }

    private void logGrpcFailure(final Throwable throwable) {
        System.err.printf(
            "gRPC error: %s (%s). Metadata: %s%n",
            throwable.getMessage(),
            throwable.getClass().getSimpleName(),
            extractMetadata(throwable)
        );
    }

    private Map<String, String> extractMetadata(final Throwable throwable) {
        Metadata metadata = null;
        if (throwable instanceof StatusException statusEx) {
            metadata = statusEx.getTrailers();
        } else if (throwable instanceof StatusRuntimeException statusEx) {
            metadata = statusEx.getTrailers();
        }
        if (metadata == null) {
            metadata = Status.trailersFromThrowable(throwable);
        }
        return extractMetadata(metadata);
    }

    private Map<String, String> extractMetadata(final Metadata metadata) {
        if (metadata == null) {
            return Map.of();
        }
        return KEYS.stream()
            .filter(metadata::containsKey)
            .filter(k -> metadata.get(k) != null)
            .map(k -> Map.entry(k.name(), metadata.get(k)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
