package com.bank.rm.risk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.risk", "com.bank.rm.common"})
public class RiskProfilingApplication {
    public static void main(String[] args) { SpringApplication.run(RiskProfilingApplication.class, args); }
}
