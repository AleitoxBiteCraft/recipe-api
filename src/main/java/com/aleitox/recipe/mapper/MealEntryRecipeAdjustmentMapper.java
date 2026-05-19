package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustment;
import com.aleitox.recipe.dto.MealEntryRecipeAdjustmentResponseDto;
import com.aleitox.recipe.entity.MealEntryRecipeAdjustmentEntity;
import org.springframework.stereotype.Component;

@Component
public class MealEntryRecipeAdjustmentMapper {

    public MealEntryRecipeAdjustment toDomain(MealEntryRecipeAdjustmentEntity entity) {
        return new MealEntryRecipeAdjustment(
                entity.getId(),
                entity.getMealEntryRecipe().getId(),
                entity.getAdjustmentType(),
                entity.getRecipeComponent() != null ? entity.getRecipeComponent().getId() : null,
                entity.getComponentType(),
                entity.getIngredient() != null ? entity.getIngredient().getId() : null,
                entity.getIngredient() != null ? entity.getIngredient().getName() : null,
                entity.getChildRecipe() != null ? entity.getChildRecipe().getId() : null,
                entity.getChildRecipe() != null ? entity.getChildRecipe().getName() : null,
                entity.getQuantity(),
                entity.getUnit(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public MealEntryRecipeAdjustmentResponseDto toResponseDto(MealEntryRecipeAdjustment domain) {
        return new MealEntryRecipeAdjustmentResponseDto(
                domain.id(),
                domain.adjustmentType(),
                domain.recipeComponentId(),
                domain.componentType(),
                domain.ingredientId(),
                domain.ingredientName(),
                domain.childRecipeId(),
                domain.childRecipeName(),
                domain.quantity(),
                domain.unit(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
