package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientUnitResponseDto(
        Integer id,
        String unit,
        BigDecimal gramsPerUnit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
