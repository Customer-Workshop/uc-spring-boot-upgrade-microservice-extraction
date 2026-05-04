package com.bank.rm.reminder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.reminder", "com.bank.rm.common"})
@EnableScheduling
public class ReminderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ReminderServiceApplication.class, args); }
}
