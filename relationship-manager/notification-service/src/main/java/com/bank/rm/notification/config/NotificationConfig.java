package com.bank.rm.notification.config;

import com.bank.rm.common.security.JwtAuthFilter;
import com.bank.rm.common.security.JwtUtil;
import com.bank.rm.notification.channel.NotificationRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class NotificationConfig {
    @Value("${jwt.secret:rm-default-secret-key-for-development-only-change-in-production-1234567890}")
    private String jwtSecret;

    @Bean
    public JwtUtil jwtUtil() { return new JwtUtil(jwtSecret); }

    @Bean
    public NotificationRouter notificationRouter() { return new NotificationRouter(); }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/notifications/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtUtil()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
