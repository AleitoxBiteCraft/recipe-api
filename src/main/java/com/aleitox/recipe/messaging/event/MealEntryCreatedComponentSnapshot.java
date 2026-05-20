package com.aleitox.recipe.messaging.event;

import java.math.BigDecimal;

public record MealEntryCreatedComponentSnapshot(
        String ingredientName,
        String childRecipeName,
        BigDecimal quantity,
        String unit
) {
}
