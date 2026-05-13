package com.aleitox.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecipeStepRequestDto(
        @NotNull
        @Positive
        Integer stepOrder,

        @NotBlank
        @Size(max = 1000)
        String description
) {
}
