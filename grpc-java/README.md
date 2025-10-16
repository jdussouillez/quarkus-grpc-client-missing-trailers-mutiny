Based on https://grpc.io/docs/languages/java/quickstart/

## Build

```sh
./gradlew installDist
```

## Run

```sh
./build/install/examples/bin/hello-world-server
```

```sh
./build/install/examples/bin/hello-world-client
```

## Results

It works with gRPC Java

Client response:

```
RPC failed (expected). Status: Status{code=INVALID_ARGUMENT, description=Invalid name, cause=null}.
    Metadata: Metadata(content-type=application/grpc,error-details=foo bar baz)
```
