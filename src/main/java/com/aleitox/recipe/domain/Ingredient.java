package com.aleitox.recipe.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Ingredient(
        Integer id,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinsPer100g,
        BigDecimal carbohydratesPer100g,
        BigDecimal fatsPer100g,
        String nutritionSource,
        List<IngredientUnit> units,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
