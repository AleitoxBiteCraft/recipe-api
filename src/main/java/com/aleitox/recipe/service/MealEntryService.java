package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.MealEntry;
import com.aleitox.recipe.domain.MealEntryRecipe;
import com.aleitox.recipe.domain.MealEntryRecipeAdjustment;
import com.aleitox.recipe.dto.MealEntryDetailResponseDto;
import com.aleitox.recipe.dto.MealEntryResponseDto;
import com.aleitox.recipe.entity.MealEntryEntity;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import com.aleitox.recipe.mapper.MealEntryMapper;
import com.aleitox.recipe.mapper.MealEntryRecipeAdjustmentMapper;
import com.aleitox.recipe.mapper.MealEntryRecipeMapper;
import com.aleitox.recipe.repository.MealEntryRecipeAdjustmentRepository;
import com.aleitox.recipe.repository.MealEntryRecipeRepository;
import com.aleitox.recipe.repository.MealEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MealEntryService {

    private final MealEntryRepository mealEntryRepository;
    private final MealEntryRecipeRepository mealEntryRecipeRepository;
    private final MealEntryRecipeAdjustmentRepository mealEntryRecipeAdjustmentRepository;
    private final MealEntryMapper mealEntryMapper;
    private final MealEntryRecipeMapper mealEntryRecipeMapper;
    private final MealEntryRecipeAdjustmentMapper mealEntryRecipeAdjustmentMapper;

    public MealEntryService(MealEntryRepository mealEntryRepository,
                            MealEntryRecipeRepository mealEntryRecipeRepository,
                            MealEntryRecipeAdjustmentRepository mealEntryRecipeAdjustmentRepository,
                            MealEntryMapper mealEntryMapper,
                            MealEntryRecipeMapper mealEntryRecipeMapper,
                            MealEntryRecipeAdjustmentMapper mealEntryRecipeAdjustmentMapper) {
        this.mealEntryRepository = mealEntryRepository;
        this.mealEntryRecipeRepository = mealEntryRecipeRepository;
        this.mealEntryRecipeAdjustmentRepository = mealEntryRecipeAdjustmentRepository;
        this.mealEntryMapper = mealEntryMapper;
        this.mealEntryRecipeMapper = mealEntryRecipeMapper;
        this.mealEntryRecipeAdjustmentMapper = mealEntryRecipeAdjustmentMapper;
    }

    public List<MealEntryResponseDto> getAll() {
        return mealEntryRepository.findAllWithDishOrderByEatenAtDesc()
                .stream()
                .map(mealEntryMapper::toDomain)
                .map(mealEntryMapper::toResponseDto)
                .toList();
    }

    public MealEntryDetailResponseDto getById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        MealEntryEntity entity = findEntityById(id);
        MealEntry mealEntry = mealEntryMapper.toDomain(entity);
        List<MealEntryRecipe> recipes = loadRecipesWithAdjustments(id);
        return mealEntryMapper.toDetailResponseDto(mealEntry, recipes);
    }

    private List<MealEntryRecipe> loadRecipesWithAdjustments(Integer mealEntryId) {
        List<MealEntryRecipeEntity> recipeEntities =
                mealEntryRecipeRepository.findByMealEntryIdWithRecipeOrderByIdAsc(mealEntryId);
        if (recipeEntities.isEmpty()) {
            return List.of();
        }

        List<Integer> mealEntryRecipeIds = recipeEntities.stream()
                .map(MealEntryRecipeEntity::getId)
                .toList();

        Map<Integer, List<MealEntryRecipeAdjustment>> adjustmentsByRecipeLineId =
                mealEntryRecipeAdjustmentRepository
                        .findByMealEntryRecipeIdInOrderByMealEntryRecipeIdAscIdAsc(mealEntryRecipeIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                adjustment -> adjustment.getMealEntryRecipe().getId(),
                                Collectors.mapping(
                                        mealEntryRecipeAdjustmentMapper::toDomain,
                                        Collectors.toList())));

        return recipeEntities.stream()
                .map(recipeEntity -> mealEntryRecipeMapper.toDomain(
                        recipeEntity,
                        adjustmentsByRecipeLineId.getOrDefault(recipeEntity.getId(), List.of())))
                .toList();
    }

    private MealEntryEntity findEntityById(Integer id) {
        return mealEntryRepository.findByIdWithDish(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meal entry not found with id: " + id));
    }
}
