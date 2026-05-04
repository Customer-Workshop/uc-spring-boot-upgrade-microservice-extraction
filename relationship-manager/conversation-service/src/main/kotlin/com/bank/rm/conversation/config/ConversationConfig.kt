package com.bank.rm.conversation.config

import com.bank.rm.common.security.JwtAuthFilter
import com.bank.rm.common.security.JwtUtil
import com.bank.rm.conversation.websocket.ChatWebSocketHandler
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value("\${jwt.secret:rm-default-secret-key-for-development-only-change-in-production-1234567890}")
    private lateinit var jwtSecret: String

    @Bean
    fun jwtUtil(): JwtUtil = JwtUtil(jwtSecret)

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/conversations").permitAll()
                    .requestMatchers("/api/v1/conversations/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthFilter(jwtUtil()), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

@Configuration
class AIConfig {

    @Value("\${ai.openai.api-key:}")
    private lateinit var openAiApiKey: String

    @Value("\${ai.openai.model:gpt-4o}")
    private lateinit var modelName: String

    @Value("\${ai.openai.base-url:https://api.openai.com/v1}")
    private lateinit var baseUrl: String

    @Bean
    fun chatLanguageModel(): ChatLanguageModel =
        OpenAiChatModel.builder()
            .apiKey(openAiApiKey.ifEmpty { "demo" })
            .modelName(modelName)
            .baseUrl(baseUrl)
            .temperature(0.7)
            .maxTokens(500)
            .build()
}

@Configuration
@EnableWebSocket
class WebSocketConfig(private val chatWebSocketHandler: ChatWebSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{conversationId}")
            .setAllowedOrigins("*")
    }
}
