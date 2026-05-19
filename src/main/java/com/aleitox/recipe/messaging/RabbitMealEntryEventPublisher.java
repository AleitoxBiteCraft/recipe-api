package com.aleitox.recipe.messaging;

import com.aleitox.recipe.dto.MealEntryResolvedComponentResponseDto;
import com.aleitox.recipe.dto.MealEntryResolvedRecipeResponseDto;
import com.aleitox.recipe.dto.MealEntryResolvedResponseDto;
import com.aleitox.recipe.messaging.event.MealEntryCreatedComponentSnapshot;
import com.aleitox.recipe.messaging.event.MealEntryCreatedEvent;
import com.aleitox.recipe.messaging.event.MealEntryCreatedRecipeSnapshot;
import com.aleitox.recipe.messaging.event.NutritionTotals;
import com.aleitox.recipe.service.MealEntryNutritionCalculator;
import com.aleitox.recipe.service.MealEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
public class RabbitMealEntryEventPublisher implements MealEntryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMealEntryEventPublisher.class);

    private final MealEntryService mealEntryService;
    private final MealEntryNutritionCalculator nutritionCalculator;
    private final RabbitTemplate rabbitTemplate;

    public RabbitMealEntryEventPublisher(MealEntryService mealEntryService,
                                         MealEntryNutritionCalculator nutritionCalculator,
                                         RabbitTemplate rabbitTemplate) {
        this.mealEntryService = mealEntryService;
        this.nutritionCalculator = nutritionCalculator;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishMealEntryCreated(Integer mealEntryId) {
        try {
            MealEntryResolvedResponseDto resolved = mealEntryService.getResolvedById(mealEntryId);
            MealEntryCreatedEvent event = toEvent(resolved);
            rabbitTemplate.convertAndSend(RabbitMqNames.MEAL_ENTRY_EVENTS_EXCHANGE, "", event);
            log.debug("Published {} for meal entry id {}", MealEntryCreatedEvent.EVENT_TYPE, mealEntryId);
        } catch (Exception ex) {
            log.error("Failed to publish meal entry created event. meal entry id: {}", mealEntryId, ex);
        }
    }

    private MealEntryCreatedEvent toEvent(MealEntryResolvedResponseDto resolved) {
        NutritionTotals mealTotals = NutritionTotals.zero();
        List<MealEntryCreatedRecipeSnapshot> recipeSnapshots = resolved.recipes().stream()
                .map(this::toRecipeSnapshot)
                .toList();
        for (MealEntryCreatedRecipeSnapshot recipe : recipeSnapshots) {
            mealTotals = mealTotals.add(recipe.nutrition());
        }
        return new MealEntryCreatedEvent(
                UUID.randomUUID(),
                MealEntryCreatedEvent.EVENT_TYPE,
                LocalDateTime.now(),
                resolved.id(),
                resolved.eatenAt(),
                resolved.dishName(),
                resolved.notes(),
                recipeSnapshots,
                mealTotals);
    }

    private MealEntryCreatedRecipeSnapshot toRecipeSnapshot(MealEntryResolvedRecipeResponseDto recipe) {
        NutritionTotals recipeNutrition = nutritionCalculator.computeForComponents(recipe.components());
        List<MealEntryCreatedComponentSnapshot> components = recipe.components().stream()
                .map(this::toComponentSnapshot)
                .toList();
        return new MealEntryCreatedRecipeSnapshot(
                recipe.name(),
                recipe.servingAmount(),
                components,
                recipeNutrition);
    }

    private MealEntryCreatedComponentSnapshot toComponentSnapshot(MealEntryResolvedComponentResponseDto component) {
        return new MealEntryCreatedComponentSnapshot(
                component.ingredientName(),
                component.childRecipeName(),
                component.quantity(),
                component.unit());
    }
}
