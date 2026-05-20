package com.aleitox.recipe.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    @Bean
    FanoutExchange mealEntryEventsExchange() {
        return new FanoutExchange(RabbitMqNames.MEAL_ENTRY_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    Queue mealEntryPocQueue() {
        return new Queue(RabbitMqNames.MEAL_ENTRY_POC_QUEUE, true);
    }

    @Bean
    Binding mealEntryPocBinding(Queue mealEntryPocQueue, FanoutExchange mealEntryEventsExchange) {
        return BindingBuilder.bind(mealEntryPocQueue).to(mealEntryEventsExchange);
    }

    @Bean
    MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
