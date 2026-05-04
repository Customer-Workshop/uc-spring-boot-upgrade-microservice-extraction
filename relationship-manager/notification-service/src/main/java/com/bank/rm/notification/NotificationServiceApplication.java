package com.bank.rm.notification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.notification", "com.bank.rm.common"})
public class NotificationServiceApplication {
    public static void main(String[] args) { SpringApplication.run(NotificationServiceApplication.class, args); }
}
