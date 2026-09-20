# java-chat-example

## How to run

Install maven dependencies with ```mvn clean install```

Then run the application:

```mvn clean spring-boot:run```

### Swagger

http://localhost:8080/swagger-ui/index.html

### H2 console

http://localhost:8080/h2-console

## Test

```shell
curl -N -X POST http://localhost:8080/api/v1/chat/stream -H "Content-Type: application/json" -d "{\"message\":\"Write a short poem about Java virtual threads\"}"
```
