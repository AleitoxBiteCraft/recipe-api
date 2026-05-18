package com.aleitox.recipe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MealEntryDetailResponseDto(
        Integer id,
        Integer dishId,
        String dishName,
        LocalDateTime eatenAt,
        String notes,
        List<MealEntryRecipeResponseDto> recipes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
