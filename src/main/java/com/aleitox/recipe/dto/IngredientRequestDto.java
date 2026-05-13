package com.aleitox.recipe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record IngredientRequestDto(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal caloriesPer100g,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal proteinsPer100g,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal carbohydratesPer100g,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal fatsPer100g,

        @Size(max = 255)
        String nutritionSource
) {
}
