package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.Ingredient;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.IngredientResponseDto;
import com.aleitox.recipe.entity.IngredientEntity;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public Ingredient toDomain(IngredientEntity entity) {
        return new Ingredient(
                entity.getId(),
                entity.getName(),
                entity.getCaloriesPer100g(),
                entity.getProteinsPer100g(),
                entity.getCarbohydratesPer100g(),
                entity.getFatsPer100g(),
                entity.getNutritionSource(),
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
        return new Ingredient(
                id,
                request.name(),
                request.caloriesPer100g(),
                request.proteinsPer100g(),
                request.carbohydratesPer100g(),
                request.fatsPer100g(),
                request.nutritionSource(),
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
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
