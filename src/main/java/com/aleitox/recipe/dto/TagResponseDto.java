package com.aleitox.recipe.dto;

import java.time.LocalDateTime;

public record TagResponseDto(
        Integer id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
