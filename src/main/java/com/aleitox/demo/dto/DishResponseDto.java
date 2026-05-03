package com.aleitox.demo.dto;

import java.time.LocalDateTime;

public record DishResponseDto(
        Integer id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
