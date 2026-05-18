package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.util.List;

public record MealEntryResolvedRecipeResponseDto(
        Integer mealEntryRecipeId,
        Integer recipeId,
        String name,
        String description,
        Integer recipeServing,
        BigDecimal servingAmount,
        List<MealEntryResolvedComponentResponseDto> components
) {
}
