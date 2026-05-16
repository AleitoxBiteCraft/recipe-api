package com.aleitox.recipe.domain;

import java.time.LocalDateTime;

public record Tag(
        Integer id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
