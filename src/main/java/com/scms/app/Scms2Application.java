package com.scms.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Scms2Application {

    public static void main(String[] args) {
        SpringApplication.run(Scms2Application.class, args);
    }

}
