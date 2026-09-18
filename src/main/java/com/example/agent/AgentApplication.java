package com.example.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApplication {

    static void main(String[] args) {
        System.out.println("Swagger: http://localhost:8080/swagger-ui/index.html");
        SpringApplication.run(AgentApplication.class, args);
    }

}
