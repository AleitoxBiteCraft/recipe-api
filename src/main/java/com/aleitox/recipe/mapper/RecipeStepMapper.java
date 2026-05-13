package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.RecipeStep;
import com.aleitox.recipe.dto.RecipeStepRequestDto;
import com.aleitox.recipe.dto.RecipeStepResponseDto;
import com.aleitox.recipe.entity.RecipeStepEntity;
import org.springframework.stereotype.Component;

@Component
public class RecipeStepMapper {

    public RecipeStep toDomain(RecipeStepEntity entity) {
        return new RecipeStep(
                entity.getId(),
                entity.getRecipe().getId(),
                entity.getStepOrder(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public RecipeStep toDomain(RecipeStepRequestDto request, Integer id, Integer recipeId) {
        return new RecipeStep(
                id,
                recipeId,
                request.stepOrder(),
                request.description(),
                null,
                null
        );
    }

    public RecipeStepResponseDto toResponseDto(RecipeStep domain) {
        return new RecipeStepResponseDto(
                domain.id(),
                domain.recipeId(),
                domain.stepOrder(),
                domain.description(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
