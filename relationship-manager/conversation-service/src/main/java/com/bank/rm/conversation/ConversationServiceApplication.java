package com.bank.rm.conversation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bank.rm.conversation", "com.bank.rm.common"})
public class ConversationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConversationServiceApplication.class, args);
    }
}
