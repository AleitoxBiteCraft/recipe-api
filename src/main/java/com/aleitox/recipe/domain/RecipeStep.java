package com.aleitox.recipe.domain;

import java.time.LocalDateTime;

public record RecipeStep(
        Integer id,
        Integer recipeId,
        Integer stepOrder,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
