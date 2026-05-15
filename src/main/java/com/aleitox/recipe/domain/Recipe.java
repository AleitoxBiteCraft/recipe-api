package com.aleitox.recipe.domain;

import java.time.LocalDateTime;

public record Recipe(
        Integer id,
        String name,
        String description,
        Integer serving,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
