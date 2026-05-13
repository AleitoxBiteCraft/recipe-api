package com.aleitox.recipe.dto;

import com.aleitox.recipe.domain.RecipeComponentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecipeComponentRequestDto(
        @NotNull
        RecipeComponentType componentType,

        @Positive
        Integer ingredientId,

        @Positive
        Integer childRecipeId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal quantity,

        @NotBlank
        @Size(max = 50)
        String unit
) {
    @AssertTrue(message = "INGREDIENT component requires ingredientId and forbids childRecipeId")
    public boolean isIngredientReferenceValid() {
        if (componentType != RecipeComponentType.INGREDIENT) {
            return true;
        }
        return ingredientId != null && childRecipeId == null;
    }

    @AssertTrue(message = "RECIPE component requires childRecipeId and forbids ingredientId")
    public boolean isRecipeReferenceValid() {
        if (componentType != RecipeComponentType.RECIPE) {
            return true;
        }
        return childRecipeId != null && ingredientId == null;
    }
}
