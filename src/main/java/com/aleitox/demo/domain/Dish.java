package com.aleitox.demo.domain;

import java.time.LocalDateTime;

public record Dish(
        Integer id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
