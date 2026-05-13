package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientResponseDto(
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
