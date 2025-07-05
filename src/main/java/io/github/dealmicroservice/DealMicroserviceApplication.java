package io.github.dealmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class DealMicroserviceApplication {

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now());
        SpringApplication.run(DealMicroserviceApplication.class, args);
    }

}
