package com.aleitox.recipe.domain;

import java.time.LocalDateTime;

public record MealEntry(
        Integer id,
        Integer dishId,
        String dishName,
        LocalDateTime eatenAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
