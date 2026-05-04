package com.bank.rm.projection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.projection", "com.bank.rm.common"})
public class ProjectionServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ProjectionServiceApplication.class, args); }
}
