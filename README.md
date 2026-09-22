# java-chat-example

## 1. Prerequisites

- In this example I use Ollama cloud model. Set your **OLLAMA_API_KEY** in the OS.

- At some point I use Postgres DB at aiven.io.

  My console: https://console.aiven.io/account/a5e0f2593af3/project/java-chat-example/services

  You should set the followings (in OS or in the .env file):
    - SPRING_DATASOURCE_URL
    - SPRING_DATASOURCE_USERNAME
    - SPRING_DATASOURCE_PASSWORD

## 2. How to run

Install maven dependencies with ```mvn clean install```

Then run the application: ```mvn clean spring-boot:run```

### Swagger

http://localhost:8080/swagger-ui/index.html

### H2 console

If you use H2, the admin console is: http://localhost:8080/h2-console

## 3. Test

```shell
curl -N -X POST http://localhost:8080/api/v1/chat/stream -H "Content-Type: application/json" -d "{\"message\":\"Write a short poem about Java virtual threads\"}"
```
