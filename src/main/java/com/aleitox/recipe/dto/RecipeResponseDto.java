package com.aleitox.recipe.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponseDto(
        Integer id,
        String name,
        String description,
        Integer serving,
        List<RecipeComponentResponseDto> components,
        List<RecipeStepResponseDto> steps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
