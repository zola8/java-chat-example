package com.example.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        System.out.println("--------------------------");
        System.out.println("Swagger: http://localhost:8080/swagger-ui/index.html");
//        System.out.println("H2 console: http://localhost:8080/h2-console");
        System.out.println("--------------------------");

        SpringApplication.run(AgentApplication.class, args);
    }

}
