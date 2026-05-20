package com.aleitox.recipe.messaging.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MealEntryCreatedEvent(
        UUID eventId,
        String eventType,
        LocalDateTime occurredAt,
        Integer mealEntryId,
        LocalDateTime eatenAt,
        String dishName,
        String notes,
        List<MealEntryCreatedRecipeSnapshot> recipes,
        NutritionTotals nutritionTotals
) {
    public static final String EVENT_TYPE = "MEAL_ENTRY_CREATED";
}
