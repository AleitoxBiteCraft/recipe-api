package com.aleitox.demo.dto;

import java.util.List;

public record DishDetailRecipeResponseDto(
        Integer id,
        String name,
        String description,
        List<DishDetailRecipeResponseDto> subRecipes
) {
}
