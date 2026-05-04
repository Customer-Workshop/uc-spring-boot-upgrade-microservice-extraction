package com.bank.rm.recommendation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.recommendation", "com.bank.rm.common"})
public class RecommendationApplication {
    public static void main(String[] args) { SpringApplication.run(RecommendationApplication.class, args); }
}
