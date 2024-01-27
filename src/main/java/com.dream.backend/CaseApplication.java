package com.dream.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CaseApplication {
    public CaseApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(CaseApplication.class, args);
    }
}
