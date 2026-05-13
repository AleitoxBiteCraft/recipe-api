package com.aleitox.recipe.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Ingredient(
        Integer id,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinsPer100g,
        BigDecimal carbohydratesPer100g,
        BigDecimal fatsPer100g,
        String nutritionSource,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
