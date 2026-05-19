package com.aleitox.recipe.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MealEntryRecipe(
        Integer id,
        Integer mealEntryId,
        Integer recipeId,
        String recipeName,
        BigDecimal servingAmount,
        List<MealEntryRecipeAdjustment> adjustments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
