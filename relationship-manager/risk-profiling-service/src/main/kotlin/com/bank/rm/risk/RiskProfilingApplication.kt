package com.bank.rm.risk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.bank.rm.risk", "com.bank.rm.common"])
class RiskProfilingApplication

fun main(args: Array<String>) {
    runApplication<RiskProfilingApplication>(*args)
}
