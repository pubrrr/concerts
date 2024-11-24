package com.bierchitekt.concerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConcertsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConcertsApplication.class, args);
    }

}
