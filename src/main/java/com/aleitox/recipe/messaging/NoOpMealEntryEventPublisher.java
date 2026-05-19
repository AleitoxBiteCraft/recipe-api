package com.aleitox.recipe.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "false", matchIfMissing = false)
public class NoOpMealEntryEventPublisher implements MealEntryEventPublisher {

    @Override
    public void publishMealEntryCreated(Integer mealEntryId) {
        // RabbitMQ disabled (e.g. test profile).
    }
}
