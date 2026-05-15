package com.aleitox.recipe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DishDetailResponseDto(
        Integer id,
        String name,
        String description,
        Integer serving,
        List<DishDetailRecipeResponseDto> recipes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
