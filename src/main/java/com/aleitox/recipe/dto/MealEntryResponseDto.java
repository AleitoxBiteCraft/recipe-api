package com.aleitox.recipe.dto;

import java.time.LocalDateTime;

public record MealEntryResponseDto(
        Integer id,
        Integer dishId,
        String dishName,
        LocalDateTime eatenAt,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
