package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MealEntryRecipeResponseDto(
        Integer id,
        Integer recipeId,
        String recipeName,
        BigDecimal servingAmount,
        List<MealEntryRecipeAdjustmentResponseDto> adjustments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
