package com.aleitox.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DishDetailResponseDto(
        Integer id,
        String name,
        String description,
        List<RecipeResponseDto> recipes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
