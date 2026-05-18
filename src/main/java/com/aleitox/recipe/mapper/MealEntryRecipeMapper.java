package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.MealEntryRecipe;
import com.aleitox.recipe.domain.MealEntryRecipeAdjustment;
import com.aleitox.recipe.dto.MealEntryRecipeResponseDto;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MealEntryRecipeMapper {

    private final MealEntryRecipeAdjustmentMapper adjustmentMapper;

    public MealEntryRecipeMapper(MealEntryRecipeAdjustmentMapper adjustmentMapper) {
        this.adjustmentMapper = adjustmentMapper;
    }

    public MealEntryRecipe toDomain(MealEntryRecipeEntity entity, List<MealEntryRecipeAdjustment> adjustments) {
        return new MealEntryRecipe(
                entity.getId(),
                entity.getMealEntry().getId(),
                entity.getRecipe().getId(),
                entity.getRecipe().getName(),
                entity.getServingAmount(),
                adjustments,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public MealEntryRecipeResponseDto toResponseDto(MealEntryRecipe domain) {
        return new MealEntryRecipeResponseDto(
                domain.id(),
                domain.recipeId(),
                domain.recipeName(),
                domain.servingAmount(),
                domain.adjustments().stream()
                        .map(adjustmentMapper::toResponseDto)
                        .toList(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
