package com.aleitox.recipe.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecipeComponent(
        Integer id,
        Integer recipeId,
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
