package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.Ingredient;
import com.aleitox.recipe.domain.IngredientUnit;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.IngredientResponseDto;
import com.aleitox.recipe.entity.IngredientEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngredientMapper {

    private final IngredientUnitMapper ingredientUnitMapper;

    public IngredientMapper(IngredientUnitMapper ingredientUnitMapper) {
        this.ingredientUnitMapper = ingredientUnitMapper;
    }

    public Ingredient toDomain(IngredientEntity entity, List<IngredientUnit> units) {
        return new Ingredient(
                entity.getId(),
                entity.getName(),
                entity.getCaloriesPer100g(),
                entity.getProteinsPer100g(),
                entity.getCarbohydratesPer100g(),
                entity.getFatsPer100g(),
                entity.getNutritionSource(),
                units,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public IngredientEntity toEntity(Ingredient domain) {
        IngredientEntity entity = new IngredientEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setCaloriesPer100g(domain.caloriesPer100g());
        entity.setProteinsPer100g(domain.proteinsPer100g());
        entity.setCarbohydratesPer100g(domain.carbohydratesPer100g());
        entity.setFatsPer100g(domain.fatsPer100g());
        entity.setNutritionSource(domain.nutritionSource());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }

    public Ingredient toDomain(IngredientRequestDto request, Integer id) {
        List<IngredientUnit> units = request.units() == null
                ? List.of()
                : request.units().stream()
                .map(unitRequest -> ingredientUnitMapper.toDomain(unitRequest, id))
                .toList();
        return new Ingredient(
                id,
                request.name(),
                request.caloriesPer100g(),
                request.proteinsPer100g(),
                request.carbohydratesPer100g(),
                request.fatsPer100g(),
                request.nutritionSource(),
                units,
                null,
                null
        );
    }

    public IngredientResponseDto toResponseDto(Ingredient domain) {
        return new IngredientResponseDto(
                domain.id(),
                domain.name(),
                domain.caloriesPer100g(),
                domain.proteinsPer100g(),
                domain.carbohydratesPer100g(),
                domain.fatsPer100g(),
                domain.nutritionSource(),
                domain.units().stream()
                        .map(ingredientUnitMapper::toResponseDto)
                        .toList(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
