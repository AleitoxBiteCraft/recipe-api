package com.aleitox.recipe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponseDto(
        Integer id,
        String name,
        String description,
        Integer serving,
        BigDecimal totalCalories,
        BigDecimal totalProteins,
        BigDecimal totalCarbohydrates,
        BigDecimal totalFats,
        List<RecipeComponentResponseDto> components,
        List<RecipeStepResponseDto> steps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
