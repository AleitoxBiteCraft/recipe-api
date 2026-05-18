package com.aleitox.recipe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MealEntryResolvedResponseDto(
        Integer id,
        Integer dishId,
        String dishName,
        LocalDateTime eatenAt,
        String notes,
        List<MealEntryResolvedRecipeResponseDto> recipes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
