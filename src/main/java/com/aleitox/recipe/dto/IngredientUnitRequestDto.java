package com.aleitox.recipe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record IngredientUnitRequestDto(
        @NotBlank
        @Size(max = 50)
        String unit,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal gramsPerUnit
) {
}
