package com.aleitox.recipe.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientUnit(
        Integer id,
        Integer ingredientId,
        String unit,
        BigDecimal gramsPerUnit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
