package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.RecipeComponent;
import com.aleitox.recipe.dto.RecipeComponentResponseDto;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import org.springframework.stereotype.Component;

@Component
public class RecipeComponentMapper {

    public RecipeComponent toDomain(RecipeComponentEntity entity) {
        return new RecipeComponent(
                entity.getId(),
                entity.getRecipe().getId(),
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

    public RecipeComponentResponseDto toResponseDto(RecipeComponent domain) {
        return new RecipeComponentResponseDto(
                domain.id(),
                domain.recipeId(),
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
