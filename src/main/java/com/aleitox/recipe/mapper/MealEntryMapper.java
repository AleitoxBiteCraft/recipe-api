package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.MealEntry;
import com.aleitox.recipe.domain.MealEntryRecipe;
import com.aleitox.recipe.dto.MealEntryDetailResponseDto;
import com.aleitox.recipe.dto.MealEntryResponseDto;
import com.aleitox.recipe.entity.MealEntryEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MealEntryMapper {

    private final MealEntryRecipeMapper mealEntryRecipeMapper;

    public MealEntryMapper(MealEntryRecipeMapper mealEntryRecipeMapper) {
        this.mealEntryRecipeMapper = mealEntryRecipeMapper;
    }

    public MealEntry toDomain(MealEntryEntity entity) {
        return new MealEntry(
                entity.getId(),
                entity.getDish().getId(),
                entity.getDish().getName(),
                entity.getEatenAt(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public MealEntryResponseDto toResponseDto(MealEntry domain) {
        return new MealEntryResponseDto(
                domain.id(),
                domain.dishId(),
                domain.dishName(),
                domain.eatenAt(),
                domain.notes(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    public MealEntryDetailResponseDto toDetailResponseDto(MealEntry domain, List<MealEntryRecipe> recipes) {
        return new MealEntryDetailResponseDto(
                domain.id(),
                domain.dishId(),
                domain.dishName(),
                domain.eatenAt(),
                domain.notes(),
                recipes.stream()
                        .map(mealEntryRecipeMapper::toResponseDto)
                        .toList(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
