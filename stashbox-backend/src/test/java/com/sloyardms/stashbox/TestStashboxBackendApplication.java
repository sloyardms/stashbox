package com.sloyardms.stashbox;

import org.springframework.boot.SpringApplication;

public class TestStashboxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(StashboxBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
