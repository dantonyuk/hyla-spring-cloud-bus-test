# Hyla Spring-Cloud-Bus Example

This is just an example of how configuration changes publishing could be implemented.

In the SpringCloud environment the applications use ConfigServer to obtain the configuration properties.
If the properties are changed on the ConfigServer, we need to restart the applications in order to pick
up the latest values. The purpose of this project is to demonstrate how to pick up configuration properties
on the applications automatically.

Apart from that automatic logging level changing is also considered.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes

### Prerequisites

In order to build and run this example, you need following application installed:

* java 8
* git
* rabbitmq

RabbitMQ should be running.

### Installating

Clone the project to your local laptop:

```bash
git clone git@github.com:dmitry-at-hyla/hyla-spring-cloud-bus-test.git
```

Open a terminal to run the config-server, build it:

```bash
cd hyla-spring-cloud-bus-test
cd configserver
./gradlew bootJar
```

Open a terminal to run the application, build it:

```bash
cd hyla-spring-cloud-bus-test
cd app
./gradlew bootJar
```

### Running the Example

Set the proper values for the rabbitmq properties in
[`configserver/src/main/resources/bootstrap.yml`](https://github.com/dmitry-at-hyla/hyla-spring-cloud-bus-test/blob/master/configserver/src/main/resources/bootstrap.yml) file.
E.g. these values are defined by default:

```yaml
spring:
  rabbitmq:
  host: localhost
  port: 5672
  username: guest
  password: guest
```

Run the config-server in the config-server terminal:

```bash
java -jar build/libs/configserver-0.0.1-SNAPSHOT.jar
```

Run the application in the application terminal:

```bash
java -jar build/libs/app-0.0.1-SNAPSHOT.jar
```

Both application and config-server should be running successfully.

## How to Test

Having everything running, call this application endpoint:

```bash
$ curl http://localhost:8080/test
{"message":"Message from ConfigServer (version 1)"}
```

You see that the message returning from the application has version 1.

Now open the [`configserver/config/application.yml`](https://github.com/dmitry-at-hyla/hyla-spring-cloud-bus-test/blob/master/configserver/config/application.yml) file and change the version to 2.
First let the config-server know about this change:

```bash
$ curl -XPOST http://localhost:8888/monitor -d 'path=*'
["*"]
```

The config-server should publish the message about this change to the RabbitMQ.
In a few seconds the application, that is listening to RabbitMQ, should get the updated
version:

```bash
$ curl http://localhost:8080/test
{"message":"Message from ConfigServer (version 2)"}
```

Note: we do not need to call all the applications in order to let them know about changes.
The only thing that should be called is ConfigServer.

## The Implementation

The project consists of two sub-projects: application and the config-server itself.
The config-server reads the configuration from the [`config/application.yml`](https://github.com/dmitry-at-hyla/hyla-spring-cloud-bus-test/blob/master/configserver/config/application.yml) file.
This file contains the only application setting: `app.message`.

After the application starts, it requests the configuration from the config-server.
There is an endpoint `/test` which responds with the value of the `app.message` property.

Note: the configuration values are bound via `@ConfigurationProperties` not `@Value`. The
latter one won't work in this case. We need to use `@RefreshScope` for it. The former one
is working without application code changes.

If some changes have been made, we call `/monitor` endpoint on the config-server side.
It gets the latest configuration and publishes the message about changes to RabbitMQ.

To support it these two dependencies should be added to the config-server project:

```gradle
implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")
implementation("org.springframework.cloud:spring-cloud-config-monitor")
```

No other changes are required for the config-server.

On the other hand, an application should include bus-amqp library:

```gradle
implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp")
```

and if we want to refresh changes per application instance, it should enable
`/refresh` endpoint. Example:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, refresh
```

## Logging Level Changing

Along with the configuration properties we are able to change logging level same way.
See [AppController.kt](https://github.com/dmitry-at-hyla/hyla-spring-cloud-bus-test/blob/master/app/src/main/kotlin/com/hylamobile/springcloudbus/app/web/AppController.kt)

[You can see](https://github.com/dmitry-at-hyla/hyla-spring-cloud-bus-test/blob/master/configserver/config/application.yml) that logging level for `com.hylamobile` is defined as `info` in `configserver/config/application.yml` file:

```yaml
logging:
  level:
    com.hylamobile: info
```

When we call `/test` endpoint, we can see that the application prints info message but not debug message:

```
... c.h.s.app.web.AppController              : AppController::infoMessage
```

Now change the value to `debug`, and call `monitor`, you can see that debug message started printing:

```
... c.h.s.app.web.AppController              : AppController::debugMessage
... c.h.s.app.web.AppController              : AppController::infoMessage
```

