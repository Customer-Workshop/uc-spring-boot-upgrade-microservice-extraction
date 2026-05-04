package com.bank.rm.conversation.config;

import com.bank.rm.common.security.JwtAuthFilter;
import com.bank.rm.common.security.JwtUtil;
import com.bank.rm.conversation.ai.ConversationAIAgent;
import com.bank.rm.conversation.ai.EntityExtractor;
import com.bank.rm.conversation.ai.PromptManager;
import com.bank.rm.conversation.engine.ConversationStateMachine;
import com.bank.rm.conversation.service.ConversationService;
import com.bank.rm.conversation.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSecurity
@EnableWebSocket
public class ConversationConfig implements WebSocketConfigurer {

    @Value("${jwt.secret:rm-default-secret-key-for-development-only-change-in-production-1234567890}")
    private String jwtSecret;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o}")
    private String modelName;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private ConversationService conversationService;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret);
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey.isEmpty() ? "demo" : openAiApiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(0.7)
                .maxTokens(500)
                .build();
    }

    @Bean
    public PromptManager promptManager() {
        return new PromptManager();
    }

    @Bean
    public ConversationAIAgent conversationAIAgent(ChatLanguageModel chatModel, PromptManager pm) {
        return new ConversationAIAgent(chatModel, pm);
    }

    @Bean
    public EntityExtractor entityExtractor(ChatLanguageModel chatModel, ObjectMapper objectMapper) {
        return new EntityExtractor(chatModel, objectMapper);
    }

    @Bean
    public ConversationStateMachine conversationStateMachine() {
        return new ConversationStateMachine();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/conversations/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtUtil()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket handler will be created after ConversationService is available
    }

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler(ConversationService conversationService,
                                                      ObjectMapper objectMapper) {
        return new ChatWebSocketHandler(conversationService, objectMapper);
    }

    @Bean
    public WebSocketConfigurer webSocketConfigurer(ChatWebSocketHandler handler) {
        return registry -> registry.addHandler(handler, "/ws/chat/*").setAllowedOrigins("*");
    }
}
