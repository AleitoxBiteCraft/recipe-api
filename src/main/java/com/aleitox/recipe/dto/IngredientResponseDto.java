package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IngredientResponseDto(
        Integer id,
        String name,
        BigDecimal caloriesPer100g,
        BigDecimal proteinsPer100g,
        BigDecimal carbohydratesPer100g,
        BigDecimal fatsPer100g,
        String nutritionSource,
        List<IngredientUnitResponseDto> units,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
