package com.aleitox.recipe.dto;

import java.time.LocalDateTime;

public record DishResponseDto(
        Integer id,
        String name,
        String description,
        Integer serving,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
