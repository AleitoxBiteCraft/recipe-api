package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.IngredientUnit;
import com.aleitox.recipe.dto.IngredientUnitRequestDto;
import com.aleitox.recipe.dto.IngredientUnitResponseDto;
import com.aleitox.recipe.entity.IngredientUnitEntity;
import org.springframework.stereotype.Component;

@Component
public class IngredientUnitMapper {

    public IngredientUnit toDomain(IngredientUnitEntity entity) {
        return new IngredientUnit(
                entity.getId(),
                entity.getIngredient().getId(),
                entity.getUnit(),
                entity.getGramsPerUnit(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public IngredientUnit toDomain(IngredientUnitRequestDto request, Integer ingredientId) {
        return new IngredientUnit(
                null,
                ingredientId,
                request.unit(),
                request.gramsPerUnit(),
                null,
                null
        );
    }

    public IngredientUnitResponseDto toResponseDto(IngredientUnit domain) {
        return new IngredientUnitResponseDto(
                domain.id(),
                domain.unit(),
                domain.gramsPerUnit(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
