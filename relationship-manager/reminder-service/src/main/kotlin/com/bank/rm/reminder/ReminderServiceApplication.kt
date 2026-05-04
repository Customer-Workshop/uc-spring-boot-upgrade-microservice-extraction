package com.bank.rm.reminder

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.bank.rm.reminder", "com.bank.rm.common"])
@EnableScheduling
class ReminderServiceApplication

fun main(args: Array<String>) {
    runApplication<ReminderServiceApplication>(*args)
}
