package com.bank.rm.followup

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.bank.rm.followup", "com.bank.rm.common"])
@EnableScheduling
class FollowUpOrchestratorApplication

fun main(args: Array<String>) {
    runApplication<FollowUpOrchestratorApplication>(*args)
}
