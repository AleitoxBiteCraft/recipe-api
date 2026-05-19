package com.aleitox.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record MealEntryRequestDto(
        @NotNull
        @Positive
        Integer dishId,

        @NotNull
        LocalDateTime eatenAt,

        @Size(max = 65535)
        String notes,

        @Valid
        List<MealEntryRecipeRequestDto> recipes
) {
}
