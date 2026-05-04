package com.bank.rm.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bank.rm.profile", "com.bank.rm.common"})
public class ProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }
}
