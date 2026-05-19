package com.aleitox.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record MealEntryRecipeRequestDto(
        @NotNull
        @Positive
        Integer recipeId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal servingAmount,

        @Valid
        List<MealEntryRecipeAdjustmentRequestDto> adjustments
) {
    public List<MealEntryRecipeAdjustmentRequestDto> adjustmentsOrEmpty() {
        return adjustments == null ? List.of() : adjustments;
    }
}
