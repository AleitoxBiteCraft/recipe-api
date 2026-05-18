package com.aleitox.recipe.dto;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MealEntryRecipeAdjustmentResponseDto(
        Integer id,
        MealEntryRecipeAdjustmentType adjustmentType,
        Integer recipeComponentId,
        RecipeComponentType componentType,
        Integer ingredientId,
        String ingredientName,
        Integer childRecipeId,
        String childRecipeName,
        BigDecimal quantity,
        String unit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
