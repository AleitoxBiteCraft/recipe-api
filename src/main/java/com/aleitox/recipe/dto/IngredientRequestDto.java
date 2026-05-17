package com.aleitox.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        String nutritionSource,

        @Valid
        List<IngredientUnitRequestDto> units
) {
    @AssertTrue(message = "Ingredient units must have unique unit codes")
    public boolean areUnitsUnique() {
        if (units == null || units.isEmpty()) {
            return true;
        }
        long distinct = units.stream()
                .map(IngredientUnitRequestDto::unit)
                .map(unit -> unit.strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet())
                .size();
        return distinct == units.size();
    }
}
