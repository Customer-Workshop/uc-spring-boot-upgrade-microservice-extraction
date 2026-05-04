package com.bank.rm.conversation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.bank.rm.conversation", "com.bank.rm.common"])
class ConversationServiceApplication

fun main(args: Array<String>) {
    runApplication<ConversationServiceApplication>(*args)
}
