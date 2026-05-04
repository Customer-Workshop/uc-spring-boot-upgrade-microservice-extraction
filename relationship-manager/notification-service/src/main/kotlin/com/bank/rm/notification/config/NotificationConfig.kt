package com.bank.rm.notification.config

import com.bank.rm.common.security.JwtAuthFilter
import com.bank.rm.common.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

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
                    .requestMatchers("/api/v1/notifications/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthFilter(jwtUtil()), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
