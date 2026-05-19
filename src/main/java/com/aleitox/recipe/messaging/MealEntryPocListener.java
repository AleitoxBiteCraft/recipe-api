package com.aleitox.recipe.messaging;

import com.aleitox.recipe.messaging.event.MealEntryCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class MealEntryPocListener {

    private static final Logger log = LoggerFactory.getLogger(MealEntryPocListener.class);

    @RabbitListener(queues = RabbitMqNames.MEAL_ENTRY_POC_QUEUE)
    public void onMealEntryCreated(MealEntryCreatedEvent event) {
        log.info(
                "Meal entry created event consumed: eventId={}, mealEntryId={}, eatenAt={}, dishName={}, nutritionTotals={}",
                event.eventId(),
                event.mealEntryId(),
                event.eatenAt(),
                event.dishName(),
                event.nutritionTotals());
    }
}
