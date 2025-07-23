package io.github.dealmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

/**
 * Главный класс приложения Deal Microservice.
 * @author Vlad1slavS
 */
@SpringBootApplication
public class DealMicroserviceApplication {

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now());
        SpringApplication.run(DealMicroserviceApplication.class, args);
    }

}
