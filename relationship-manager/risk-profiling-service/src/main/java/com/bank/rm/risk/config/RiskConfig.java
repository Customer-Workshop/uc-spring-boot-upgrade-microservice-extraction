package com.bank.rm.risk.config;

import com.bank.rm.common.security.JwtAuthFilter;
import com.bank.rm.common.security.JwtUtil;
import com.bank.rm.risk.service.RiskScoringEngine;
import com.bank.rm.risk.service.ShapExplanationService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
public class RiskConfig {

    @Value("${jwt.secret:rm-default-secret-key-for-development-only-change-in-production-1234567890}")
    private String jwtSecret;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o}")
    private String modelName;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Bean
    public JwtUtil jwtUtil() { return new JwtUtil(jwtSecret); }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey.isEmpty() ? "demo" : openAiApiKey)
                .modelName(modelName).baseUrl(baseUrl).temperature(0.3).maxTokens(300).build();
    }

    @Bean
    public RiskScoringEngine riskScoringEngine() { return new RiskScoringEngine(); }

    @Bean
    public ShapExplanationService shapExplanationService(ChatLanguageModel chatModel) {
        return new ShapExplanationService(chatModel);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/risk/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtUtil()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
