package io.github.dealmicroservice;

import org.springframework.boot.SpringApplication;

public class TestDealMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.from(DealMicroserviceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
