package com.bank.rm.followup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.followup", "com.bank.rm.common"})
@EnableScheduling
public class FollowUpOrchestratorApplication {
    public static void main(String[] args) { SpringApplication.run(FollowUpOrchestratorApplication.class, args); }
}
