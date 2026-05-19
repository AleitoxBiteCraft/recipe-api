package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.MealEntry;
import com.aleitox.recipe.dto.MealEntryResolvedComponentResponseDto;
import com.aleitox.recipe.dto.MealEntryResolvedRecipeResponseDto;
import com.aleitox.recipe.dto.MealEntryResolvedResponseDto;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MealEntryResolvedMapper {

    public MealEntryResolvedResponseDto toResponseDto(
            MealEntry mealEntry,
            List<MealEntryResolvedRecipeResponseDto> recipes) {
        return new MealEntryResolvedResponseDto(
                mealEntry.id(),
                mealEntry.dishId(),
                mealEntry.dishName(),
                mealEntry.eatenAt(),
                mealEntry.notes(),
                recipes,
                mealEntry.createdAt(),
                mealEntry.updatedAt()
        );
    }

    public MealEntryResolvedRecipeResponseDto toRecipeResponseDto(
            MealEntryRecipeEntity mealEntryRecipe,
            List<MealEntryResolvedComponentResponseDto> components) {
        var recipe = mealEntryRecipe.getRecipe();
        return new MealEntryResolvedRecipeResponseDto(
                mealEntryRecipe.getId(),
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getServing(),
                mealEntryRecipe.getServingAmount(),
                components
        );
    }
}
