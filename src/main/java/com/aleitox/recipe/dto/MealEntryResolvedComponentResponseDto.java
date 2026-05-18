package com.aleitox.recipe.dto;

import com.aleitox.recipe.domain.RecipeComponentType;

import java.math.BigDecimal;

public record MealEntryResolvedComponentResponseDto(
        RecipeComponentType componentType,
        Integer ingredientId,
        String ingredientName,
        Integer childRecipeId,
        String childRecipeName,
        BigDecimal quantity,
        String unit
) {
}
