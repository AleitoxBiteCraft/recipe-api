package com.aleitox.recipe.messaging.event;

import java.math.BigDecimal;
import java.util.List;

public record MealEntryCreatedRecipeSnapshot(
        String recipeName,
        BigDecimal servingAmount,
        List<MealEntryCreatedComponentSnapshot> components,
        NutritionTotals nutrition
) {
}
