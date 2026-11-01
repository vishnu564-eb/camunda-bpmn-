package com.camunda.labs;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = "classpath*:/models/*.*")
public class CamundaLabsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamundaLabsApplication.class, args);
    }
}
