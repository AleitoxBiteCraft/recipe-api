package com.aleitox.recipe.dto;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MealEntryRecipeAdjustmentRequestDto(
        @NotNull
        MealEntryRecipeAdjustmentType adjustmentType,

        @Positive
        Integer recipeComponentId,

        RecipeComponentType componentType,

        @Positive
        Integer ingredientId,

        @Positive
        Integer childRecipeId,

        @DecimalMin(value = "0.01")
        BigDecimal quantity,

        @Size(max = 50)
        String unit
) {
    @AssertTrue(message = "REMOVE adjustment requires recipeComponentId only")
    public boolean isRemoveValid() {
        if (adjustmentType != MealEntryRecipeAdjustmentType.REMOVE) {
            return true;
        }
        return recipeComponentId != null
                && componentType == null
                && ingredientId == null
                && childRecipeId == null
                && quantity == null
                && unit == null;
    }

    @AssertTrue(message = "ADD adjustment requires componentType, quantity, unit, and matching reference")
    public boolean isAddValid() {
        if (adjustmentType != MealEntryRecipeAdjustmentType.ADD) {
            return true;
        }
        if (recipeComponentId != null || quantity == null || unit == null || unit.isBlank()) {
            return false;
        }
        if (componentType == RecipeComponentType.INGREDIENT) {
            return ingredientId != null && childRecipeId == null;
        }
        if (componentType == RecipeComponentType.RECIPE) {
            return childRecipeId != null && ingredientId == null;
        }
        return false;
    }
}
