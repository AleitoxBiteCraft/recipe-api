package com.aleitox.recipe.messaging;

public interface MealEntryEventPublisher {

    void publishMealEntryCreated(Integer mealEntryId);
}
