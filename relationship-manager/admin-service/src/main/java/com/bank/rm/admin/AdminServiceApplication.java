package com.bank.rm.admin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = {"com.bank.rm.admin", "com.bank.rm.common"})
public class AdminServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AdminServiceApplication.class, args); }
}
