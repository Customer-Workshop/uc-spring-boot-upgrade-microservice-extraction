package com.bank.rm.projection

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.bank.rm.projection", "com.bank.rm.common"])
class WealthProjectionApplication

fun main(args: Array<String>) {
    runApplication<WealthProjectionApplication>(*args)
}
