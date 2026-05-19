package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustment;
import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.dto.MealEntryResolvedComponentResponseDto;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import com.aleitox.recipe.repository.RecipeComponentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MealEntryRecipeCompositionResolver {

    private static final int QUANTITY_SCALE = 2;
    private static final RoundingMode QUANTITY_ROUNDING = RoundingMode.HALF_UP;

    private final RecipeComponentRepository recipeComponentRepository;

    public MealEntryRecipeCompositionResolver(RecipeComponentRepository recipeComponentRepository) {
        this.recipeComponentRepository = recipeComponentRepository;
    }

    public List<MealEntryResolvedComponentResponseDto> resolve(MealEntryRecipeEntity mealEntryRecipe,
                                                               List<MealEntryRecipeAdjustment> adjustments) {
        Set<Integer> removedComponentIds = new HashSet<>();
        List<MealEntryRecipeAdjustment> additions = new ArrayList<>();

        for (MealEntryRecipeAdjustment adjustment : adjustments) {
            if (adjustment.adjustmentType() == MealEntryRecipeAdjustmentType.REMOVE) {
                removedComponentIds.add(adjustment.recipeComponentId());
            } else if (adjustment.adjustmentType() == MealEntryRecipeAdjustmentType.ADD) {
                additions.add(adjustment);
            }
        }

        List<MealEntryResolvedComponentResponseDto> components = new ArrayList<>();
        Integer recipeId = mealEntryRecipe.getRecipe().getId();

        for (RecipeComponentEntity catalogComponent :
                recipeComponentRepository.findByRecipeIdWithReferencesOrderByIdAsc(recipeId)) {
            if (!removedComponentIds.contains(catalogComponent.getId())) {
                components.add(fromCatalogComponent(catalogComponent));
            }
        }

        for (MealEntryRecipeAdjustment addition : additions) {
            components.add(fromAddition(addition));
        }

        BigDecimal scaleFactor = servingScaleFactor(
                mealEntryRecipe.getServingAmount(),
                mealEntryRecipe.getRecipe().getServing());

        return components.stream()
                .map(component -> scaleComponent(component, scaleFactor))
                .toList();
    }

    private static BigDecimal servingScaleFactor(BigDecimal servingAmount, Integer recipeServing) {
        if (recipeServing != null && recipeServing > 0) {
            return servingAmount.divide(BigDecimal.valueOf(recipeServing), 8, QUANTITY_ROUNDING);
        }
        return servingAmount;
    }

    private static MealEntryResolvedComponentResponseDto fromCatalogComponent(RecipeComponentEntity entity) {
        return new MealEntryResolvedComponentResponseDto(
                entity.getComponentType(),
                entity.getIngredient() != null ? entity.getIngredient().getId() : null,
                entity.getIngredient() != null ? entity.getIngredient().getName() : null,
                entity.getChildRecipe() != null ? entity.getChildRecipe().getId() : null,
                entity.getChildRecipe() != null ? entity.getChildRecipe().getName() : null,
                entity.getQuantity(),
                entity.getUnit()
        );
    }

    private static MealEntryResolvedComponentResponseDto fromAddition(MealEntryRecipeAdjustment adjustment) {
        return new MealEntryResolvedComponentResponseDto(
                adjustment.componentType(),
                adjustment.ingredientId(),
                adjustment.ingredientName(),
                adjustment.childRecipeId(),
                adjustment.childRecipeName(),
                adjustment.quantity(),
                adjustment.unit()
        );
    }

    private static MealEntryResolvedComponentResponseDto scaleComponent(
            MealEntryResolvedComponentResponseDto component,
            BigDecimal scaleFactor) {
        if (scaleFactor.compareTo(BigDecimal.ONE) == 0) {
            return component;
        }
        if (component.componentType() == RecipeComponentType.RECIPE && "batch".equals(component.unit())) {
            return new MealEntryResolvedComponentResponseDto(
                    component.componentType(),
                    component.ingredientId(),
                    component.ingredientName(),
                    component.childRecipeId(),
                    component.childRecipeName(),
                    component.quantity().multiply(scaleFactor).setScale(QUANTITY_SCALE, QUANTITY_ROUNDING),
                    component.unit()
            );
        }
        return new MealEntryResolvedComponentResponseDto(
                component.componentType(),
                component.ingredientId(),
                component.ingredientName(),
                component.childRecipeId(),
                component.childRecipeName(),
                component.quantity().multiply(scaleFactor).setScale(QUANTITY_SCALE, QUANTITY_ROUNDING),
                component.unit()
        );
    }
}
