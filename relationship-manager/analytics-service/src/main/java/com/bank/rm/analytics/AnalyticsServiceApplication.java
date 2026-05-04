package com.bank.rm.analytics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.analytics", "com.bank.rm.common"})
public class AnalyticsServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AnalyticsServiceApplication.class, args); }
}
