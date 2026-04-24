package com.sure.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter authForwardingFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader != null) {
                exchange = exchange.mutate()
                        .request(request.mutate()
                                .header("Authorization", authHeader)
                                .build())
                        .build();
            }
            return chain.filter(exchange).then(Mono.empty());
        };
    }
}
