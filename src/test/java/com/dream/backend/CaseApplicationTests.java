package com.dream.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CaseApplicationTests {
    public CaseApplicationTests() {
    }

    public static void main(String[] args) {
        SpringApplication.run(CaseApplicationTests.class, args);
    }
}
