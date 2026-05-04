package com.bank.rm.common.config;

import com.bank.rm.common.event.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic conversationEvents() {
        return TopicBuilder.name(KafkaTopics.CONVERSATION_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic profileEvents() {
        return TopicBuilder.name(KafkaTopics.PROFILE_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic riskEvents() {
        return TopicBuilder.name(KafkaTopics.RISK_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic recommendationEvents() {
        return TopicBuilder.name(KafkaTopics.RECOMMENDATION_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic followupEvents() {
        return TopicBuilder.name(KafkaTopics.FOLLOWUP_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic reminderEvents() {
        return TopicBuilder.name(KafkaTopics.REMINDER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationEvents() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic productEvents() {
        return TopicBuilder.name(KafkaTopics.PRODUCT_EVENTS).partitions(3).replicas(1).build();
    }
}
