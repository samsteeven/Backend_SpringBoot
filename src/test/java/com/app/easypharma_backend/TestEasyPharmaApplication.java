package com.app.easypharma_backend;

import org.springframework.boot.SpringApplication;

public class TestEasyPharmaApplication {

    public static void main(String[] args) {
        SpringApplication.from(EasyPharmaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
