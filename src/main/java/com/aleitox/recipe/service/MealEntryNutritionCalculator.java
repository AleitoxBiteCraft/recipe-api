package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.dto.MealEntryResolvedComponentResponseDto;
import com.aleitox.recipe.entity.IngredientEntity;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import com.aleitox.recipe.messaging.event.NutritionTotals;
import com.aleitox.recipe.repository.IngredientRepository;
import com.aleitox.recipe.repository.IngredientUnitRepository;
import com.aleitox.recipe.repository.RecipeComponentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class MealEntryNutritionCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int NUTRITION_SCALE = 4;
    private static final int RESPONSE_NUTRITION_DECIMALS = 2;
    private static final RoundingMode NUTRITION_ROUNDING = RoundingMode.HALF_UP;

    private final IngredientRepository ingredientRepository;
    private final IngredientUnitRepository ingredientUnitRepository;
    private final RecipeComponentRepository recipeComponentRepository;

    public MealEntryNutritionCalculator(IngredientRepository ingredientRepository,
                                        IngredientUnitRepository ingredientUnitRepository,
                                        RecipeComponentRepository recipeComponentRepository) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientUnitRepository = ingredientUnitRepository;
        this.recipeComponentRepository = recipeComponentRepository;
    }

    public NutritionTotals computeForComponents(List<MealEntryResolvedComponentResponseDto> components) {
        BatchTotals totals = BatchTotals.zero();
        for (MealEntryResolvedComponentResponseDto component : components) {
            totals = totals.add(resolvedComponentTotals(component, new HashSet<>()));
        }
        return toNutritionTotals(totals);
    }

    private BatchTotals resolvedComponentTotals(MealEntryResolvedComponentResponseDto component,
                                                Set<Integer> visiting) {
        if (component.componentType() == RecipeComponentType.INGREDIENT) {
            if (component.ingredientId() == null) {
                return BatchTotals.zero();
            }
            IngredientEntity ingredient = ingredientRepository.findById(component.ingredientId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Ingredient not found with id: " + component.ingredientId()));
            BigDecimal grams = ingredientGrams(
                    component.quantity(),
                    component.unit(),
                    component.ingredientId());
            return new BatchTotals(
                    scalePer100g(ingredient.getCaloriesPer100g(), grams),
                    scalePer100g(ingredient.getProteinsPer100g(), grams),
                    scalePer100g(ingredient.getCarbohydratesPer100g(), grams),
                    scalePer100g(ingredient.getFatsPer100g(), grams),
                    grams);
        }
        if (component.componentType() == RecipeComponentType.RECIPE && component.childRecipeId() != null) {
            BatchTotals childBatch = computeBatchTotals(component.childRecipeId(), visiting);
            String unit = component.unit();
            BigDecimal quantity = component.quantity();
            if (unit == null || quantity == null) {
                return BatchTotals.zero();
            }
            if ("batch".equalsIgnoreCase(unit.strip())) {
                return childBatch.scale(quantity);
            }
            BigDecimal grams = ingredientGrams(quantity, unit, null);
            return childBatch.scaleToMass(grams);
        }
        return BatchTotals.zero();
    }

    private BatchTotals computeBatchTotals(Integer recipeId, Set<Integer> visiting) {
        if (!visiting.add(recipeId)) {
            return BatchTotals.zero();
        }
        try {
            BatchTotals acc = BatchTotals.zero();
            for (RecipeComponentEntity catalogComponent :
                    recipeComponentRepository.findByRecipeIdWithReferencesOrderByIdAsc(recipeId)) {
                acc = acc.add(catalogComponentTotals(catalogComponent, visiting));
            }
            return acc;
        } finally {
            visiting.remove(recipeId);
        }
    }

    private BatchTotals catalogComponentTotals(RecipeComponentEntity component, Set<Integer> visiting) {
        if (component.getComponentType() == RecipeComponentType.INGREDIENT) {
            if (component.getIngredient() == null) {
                return BatchTotals.zero();
            }
            BigDecimal grams = ingredientGrams(
                    component.getQuantity(),
                    component.getUnit(),
                    component.getIngredient().getId());
            IngredientEntity ingredient = component.getIngredient();
            return new BatchTotals(
                    scalePer100g(ingredient.getCaloriesPer100g(), grams),
                    scalePer100g(ingredient.getProteinsPer100g(), grams),
                    scalePer100g(ingredient.getCarbohydratesPer100g(), grams),
                    scalePer100g(ingredient.getFatsPer100g(), grams),
                    grams);
        }
        if (component.getComponentType() == RecipeComponentType.RECIPE && component.getChildRecipe() != null) {
            Integer childId = component.getChildRecipe().getId();
            BatchTotals childBatch = computeBatchTotals(childId, visiting);
            String unit = component.getUnit();
            BigDecimal quantity = component.getQuantity();
            if (unit == null || quantity == null) {
                return BatchTotals.zero();
            }
            if ("batch".equalsIgnoreCase(unit.strip())) {
                return childBatch.scale(quantity);
            }
            BigDecimal grams = ingredientGrams(quantity, unit, null);
            return childBatch.scaleToMass(grams);
        }
        return BatchTotals.zero();
    }

    private BigDecimal ingredientGrams(BigDecimal quantity, String unit, Integer ingredientId) {
        if (quantity == null || unit == null) {
            return BigDecimal.ZERO;
        }
        String normalizedUnit = unit.strip();
        if (isMassOrVolumeUnit(normalizedUnit)) {
            return quantity;
        }
        if (ingredientId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported component unit for nutrition: \"" + unit + "\" (expected g, ml, or batch for nested recipes)");
        }
        return ingredientUnitRepository.findByIngredientIdAndUnitNormalized(ingredientId, normalizedUnit)
                .map(conversion -> quantity.multiply(conversion.getGramsPerUnit()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No grams-per-unit conversion for ingredient id " + ingredientId + " and unit \"" + unit + "\""));
    }

    private static boolean isMassOrVolumeUnit(String unit) {
        return "g".equalsIgnoreCase(unit) || "ml".equalsIgnoreCase(unit);
    }

    private static BigDecimal scalePer100g(BigDecimal per100g, BigDecimal grams) {
        if (per100g == null || grams.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return per100g.multiply(grams).divide(HUNDRED, NUTRITION_SCALE, NUTRITION_ROUNDING);
    }

    private NutritionTotals toNutritionTotals(BatchTotals totals) {
        return new NutritionTotals(
                roundForResponse(totals.calories()),
                roundForResponse(totals.proteins()),
                roundForResponse(totals.carbohydrates()),
                roundForResponse(totals.fats()));
    }

    private static BigDecimal roundForResponse(BigDecimal value) {
        return value.setScale(RESPONSE_NUTRITION_DECIMALS, NUTRITION_ROUNDING);
    }

    private record BatchTotals(
            BigDecimal calories,
            BigDecimal proteins,
            BigDecimal carbohydrates,
            BigDecimal fats,
            BigDecimal massGrams
    ) {
        static BatchTotals zero() {
            return new BatchTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BatchTotals add(BatchTotals other) {
            return new BatchTotals(
                    calories.add(other.calories()),
                    proteins.add(other.proteins()),
                    carbohydrates.add(other.carbohydrates()),
                    fats.add(other.fats()),
                    massGrams.add(other.massGrams()));
        }

        BatchTotals scale(BigDecimal factor) {
            if (factor == null || factor.signum() == 0) {
                return zero();
            }
            return new BatchTotals(
                    calories.multiply(factor),
                    proteins.multiply(factor),
                    carbohydrates.multiply(factor),
                    fats.multiply(factor),
                    massGrams.multiply(factor));
        }

        BatchTotals scaleToMass(BigDecimal grams) {
            if (grams == null || grams.signum() == 0) {
                return zero();
            }
            if (massGrams.signum() == 0) {
                return new BatchTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, grams);
            }
            BigDecimal ratio = grams.divide(massGrams, NUTRITION_SCALE + 4, NUTRITION_ROUNDING);
            return new BatchTotals(
                    calories.multiply(ratio),
                    proteins.multiply(ratio),
                    carbohydrates.multiply(ratio),
                    fats.multiply(ratio),
                    grams);
        }
    }
}
