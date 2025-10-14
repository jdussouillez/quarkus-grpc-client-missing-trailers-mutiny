# quarkus-grpc-client-missing-trailers-mutiny

Project to reproduce a bug where gRPC trailers are lost in gRPC client with Mutiny.

See https://github.com/quarkusio/quarkus/issues/45248

## How to run

Start both gRPC servers and the gRPC client. The client will send the requests automatically.

```sh
cd grpc-server
./mvnw quarkus:dev
```

```sh
cd grpc-client
./mvnw quarkus:dev
```

The client should be failing. But we should have the metadata in the log, but we don't.

Current log:
```
gRPC error: INVALID_ARGUMENT: Name 'error' is invalid (StatusRuntimeException). Metadata: {}
```

Expected log:

```
gRPC error: UNAVAILABLE (StatusRuntimeException). Metadata: {x-exception=java.lang.IllegalArgumentException,x-trace-id=26d869b6-d6e1-4c74-8955-1d9376944d7d}
```

## Workaround

None.

## Testing with grpcurl

To check that the server is sending the trailers/metadata, use [`grpcurl`](https://github.com/fullstorydev/grpcurl).
```sh
grpcurl \
    -v \
    -plaintext \
    -import-path . \
    -proto hello.proto \
    -d '{"name": "error"}' \
    localhost:8081 \
    com.github.jdussouillez.hello.HelloGrpcApi/SayHello
```

Output:

```
Response trailers received:
content-length: 0
content-type: application/grpc
grpc-accept-encoding: gzip
x-exception: java.lang.IllegalArgumentException
x-trace-id: b3e1158f-8d7f-4909-a188-925b57e7dc09
Sent 1 request and received 0 responses
ERROR:
  Code: InvalidArgument
  Message: Name 'error' is invalid
```
