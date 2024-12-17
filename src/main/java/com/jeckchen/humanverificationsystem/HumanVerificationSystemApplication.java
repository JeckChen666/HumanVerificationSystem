package com.jeckchen.humanverificationsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HumanVerificationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HumanVerificationSystemApplication.class, args);
    }

}
