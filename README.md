# UID API

A proof of concept micro service used to generate unique IDs in a given 
infrastructure.

## Description

The service provide a simple REST api endpoint that accept a namespace, and return an ID guaranteed 
to be unique within that namespace. Two generators are provided:

1) Twitter snowflake inspired generator
2) UUID v4 based generator

The implementation defaults to Snowflake, but can be swapped to UUID v4 generator with a 
configuration change.

In order to maximize generation speed and minimize storage requirement, no attempt is made to 
persist these IDs to validate uniqueness. Uniqueness is guaranteed by the nature of each generator.

The namespace accepted must be between 1 and 50 characters in the range [a-zA-Z0-9_].

The API endpoint is defined as: 

Request:

    GET /uidapi/v1/uid/:namespace
    
Response:

    {
      "uid" : "<namespace>-<id>"
    }
    
Sample valid request and output:

```
curl -i "http://<host>:<port>>/uidapi/v1/uid/foo"

HTTP/1.1 200 OK
content-length: 41
x-response-time: 1ms
content-type: application/json

{
  "uid" : "foo-2572784626593792"
}
```

Invalid request and output:

```
curl -i "http://localhost:9999/uidapi/v1/uid/x-x"

HTTP/1.1 400 Bad Request
content-length: 90
x-response-time: 0ms
content-type: application/json

{
  "message" : "Namespace must include only characters in [a-zA-Z_0-9]",
  "code" : 400
}
```

## Pros and Cons

### Snowflake

Snowflake is the fastest of both generators. Generation of a unique ID is based on a time component,
a sequence id and a worker id. These components are merged together in a 64 bits integer using 
the following bit pattern:

| Time (42 bits) | Worker id (10 bits) | Sequence id (12 bits) |

This integer is converted to an unsigned long string representation and prefixed by the namespace 
provided before being returned in th response.

The time component is in milli seconds relative to a fixed recent epoch (01/01/2020 00:00:00Z) in 
order to maximize usefulness of the bits. The sequence id is used in case generation of two 
consecutive IDs is inside the same millisecond. In that case, the sequence is incremented by 1 for 
each collisions, and reset to 0 when a generation occurs a milliseconds or more later. 12 bits of 
sequence allows for 4096 IDs generated in 1 ms for a single generator. In the event this sequence 
max value is reached, a busy loop will wait to the next millisecond to reset the sequence and return
a new ID.

The worker id is used to disambiguate multiple instances of the service deployed within one's 
infrastructure. It allows for 1024 instances of the generator. 

The drawback of this generator is that careful allocation of the worker IDs must be observed. These
IDs are also unique within one infrastructure and not globally unique.

ID generation is fast, and the nature of the id make it so that they are generated roughly in order 
and are thus minimizing index maintenance when used as keys.  

The snowflake generator does not use synchronized block as each generator instance is 
guaranteed to be used from a single thread at a time by vertx. This lead to highly concurrent 
non-blocking approach. 

### UUID v4

The UUID v4 based generator is quite simple. It delegates ID generation to Java's UUID v4 
implementation. Although they are globally unique in nature, they make use of a secure 
random number generator accessed in a synchronized fashion, leading to contention in 
multi-threaded context.

## Frameworks used

The project is based on the vert.x framework (https://vertx.io/). This framework is well suited to
kind of service. It uses a thread per cpu core model using non blocking io from netty and an event driven approach. Provided an event 
handling thread is never blocked, it provide a high level of concurrency with minimal resource 
consumption. A thread per request pattern does not scale well to thousands of concurrent requests.
 
Google guice is used for dependency injection, as vert.x does not provide an integrated dependency 
inversion container.

logback is used for logging using json formatter and a console appender

Dropwizard metrics are exported to logs in a periodic fashion.

## Building and running

The project is Maven based and depends in JDK 11. Tested with AdoptOpenJDK (build 11.0.5+10)

To build and run tests:

    mvn package

This will produce the following jar file: `./web/target/web-1.0.0-fat.jar` which is fat jar 
containing a launcher starting the vertx web server.

To run the project with default configuration values:

    java -jar ./web/target/web-1.0.0-fat.jar
    
## Configuration

The `web/src/main/resources/default.properties` file contains default property. These can be 
overridden using environment variables (great for container deployments). 

For example using bash or zsh:

    export SERVER_PORT=7777
    java -jar ./web/target/web-1.0.0-fat.jar
    
The property file can also be defined in in `/etc/uidgen/default.properties` filesystem location.

To use UUIDv4 generator instead of Snowflake, override `UID_GENERATOR`:

    export UID_GENERATOR=uuid
    export SERVER_PORT=7777
    java -jar ./web/target/web-1.0.0-fat.jar

