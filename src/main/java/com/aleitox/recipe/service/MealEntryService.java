package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.MealEntry;
import com.aleitox.recipe.domain.MealEntryRecipe;
import com.aleitox.recipe.domain.MealEntryRecipeAdjustment;
import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.dto.MealEntryDetailResponseDto;
import com.aleitox.recipe.dto.MealEntryRecipeAdjustmentRequestDto;
import com.aleitox.recipe.dto.MealEntryRecipeRequestDto;
import com.aleitox.recipe.dto.MealEntryRequestDto;
import com.aleitox.recipe.dto.MealEntryResolvedResponseDto;
import com.aleitox.recipe.dto.MealEntryResponseDto;
import com.aleitox.recipe.entity.DishEntity;
import com.aleitox.recipe.entity.IngredientEntity;
import com.aleitox.recipe.entity.MealEntryEntity;
import com.aleitox.recipe.entity.MealEntryRecipeAdjustmentEntity;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import com.aleitox.recipe.entity.RecipeEntity;
import com.aleitox.recipe.mapper.MealEntryMapper;
import com.aleitox.recipe.mapper.MealEntryRecipeAdjustmentMapper;
import com.aleitox.recipe.mapper.MealEntryRecipeMapper;
import com.aleitox.recipe.mapper.MealEntryResolvedMapper;
import com.aleitox.recipe.repository.DishRecipeRepository;
import com.aleitox.recipe.repository.DishRepository;
import com.aleitox.recipe.repository.IngredientRepository;
import com.aleitox.recipe.repository.IngredientUnitRepository;
import com.aleitox.recipe.repository.MealEntryRecipeAdjustmentRepository;
import com.aleitox.recipe.repository.MealEntryRecipeRepository;
import com.aleitox.recipe.repository.MealEntryRepository;
import com.aleitox.recipe.repository.RecipeComponentRepository;
import com.aleitox.recipe.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MealEntryService {

    private static final BigDecimal DEFAULT_SERVING_AMOUNT = BigDecimal.ONE;

    private final MealEntryRepository mealEntryRepository;
    private final MealEntryRecipeRepository mealEntryRecipeRepository;
    private final MealEntryRecipeAdjustmentRepository mealEntryRecipeAdjustmentRepository;
    private final DishRepository dishRepository;
    private final DishRecipeRepository dishRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientUnitRepository ingredientUnitRepository;
    private final MealEntryMapper mealEntryMapper;
    private final MealEntryRecipeMapper mealEntryRecipeMapper;
    private final MealEntryRecipeAdjustmentMapper mealEntryRecipeAdjustmentMapper;
    private final MealEntryResolvedMapper mealEntryResolvedMapper;
    private final MealEntryRecipeCompositionResolver compositionResolver;

    public MealEntryService(MealEntryRepository mealEntryRepository,
                            MealEntryRecipeRepository mealEntryRecipeRepository,
                            MealEntryRecipeAdjustmentRepository mealEntryRecipeAdjustmentRepository,
                            DishRepository dishRepository,
                            DishRecipeRepository dishRecipeRepository,
                            RecipeRepository recipeRepository,
                            RecipeComponentRepository recipeComponentRepository,
                            IngredientRepository ingredientRepository,
                            IngredientUnitRepository ingredientUnitRepository,
                            MealEntryMapper mealEntryMapper,
                            MealEntryRecipeMapper mealEntryRecipeMapper,
                            MealEntryRecipeAdjustmentMapper mealEntryRecipeAdjustmentMapper,
                            MealEntryResolvedMapper mealEntryResolvedMapper,
                            MealEntryRecipeCompositionResolver compositionResolver) {
        this.mealEntryRepository = mealEntryRepository;
        this.mealEntryRecipeRepository = mealEntryRecipeRepository;
        this.mealEntryRecipeAdjustmentRepository = mealEntryRecipeAdjustmentRepository;
        this.dishRepository = dishRepository;
        this.dishRecipeRepository = dishRecipeRepository;
        this.recipeRepository = recipeRepository;
        this.recipeComponentRepository = recipeComponentRepository;
        this.ingredientRepository = ingredientRepository;
        this.ingredientUnitRepository = ingredientUnitRepository;
        this.mealEntryMapper = mealEntryMapper;
        this.mealEntryRecipeMapper = mealEntryRecipeMapper;
        this.mealEntryRecipeAdjustmentMapper = mealEntryRecipeAdjustmentMapper;
        this.mealEntryResolvedMapper = mealEntryResolvedMapper;
        this.compositionResolver = compositionResolver;
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

    public MealEntryResolvedResponseDto getResolvedById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        MealEntryEntity entity = findEntityById(id);
        MealEntry mealEntry = mealEntryMapper.toDomain(entity);
        List<MealEntryRecipeEntity> recipeEntities =
                mealEntryRecipeRepository.findByMealEntryIdWithRecipeOrderByIdAsc(id);
        Map<Integer, List<MealEntryRecipeAdjustment>> adjustmentsByRecipeLineId =
                loadAdjustmentsByMealEntryRecipeLineId(recipeEntities);

        var resolvedRecipes = recipeEntities.stream()
                .map(recipeEntity -> mealEntryResolvedMapper.toRecipeResponseDto(
                        recipeEntity,
                        compositionResolver.resolve(
                                recipeEntity,
                                adjustmentsByRecipeLineId.getOrDefault(recipeEntity.getId(), List.of()))))
                .toList();

        return mealEntryResolvedMapper.toResponseDto(mealEntry, resolvedRecipes);
    }

    @Transactional
    public MealEntryDetailResponseDto create(MealEntryRequestDto request) {
        DishEntity dish = findDishById(request.dishId());
        List<Integer> dishRecipeIds = dishRecipeRepository.findRecipeIdsByDishIdOrderByLinkIdAsc(dish.getId());
        if (dishRecipeIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dish has no linked recipes. dish id: " + dish.getId());
        }

        List<MealEntryRecipeRequestDto> recipeRequests = resolveRecipeRequests(request.recipes(), dishRecipeIds);

        LocalDateTime now = LocalDateTime.now();
        MealEntryEntity mealEntry = new MealEntryEntity();
        mealEntry.setDish(dish);
        mealEntry.setEatenAt(request.eatenAt());
        mealEntry.setNotes(request.notes());
        mealEntry.setCreatedAt(now);
        mealEntry.setUpdatedAt(now);
        MealEntryEntity savedEntry = mealEntryRepository.save(mealEntry);

        for (MealEntryRecipeRequestDto recipeRequest : recipeRequests) {
            validateRecipeBelongsToDish(dish.getId(), recipeRequest.recipeId());
            RecipeEntity recipe = findRecipeById(recipeRequest.recipeId());

            MealEntryRecipeEntity mealEntryRecipe = new MealEntryRecipeEntity();
            mealEntryRecipe.setMealEntry(savedEntry);
            mealEntryRecipe.setRecipe(recipe);
            mealEntryRecipe.setServingAmount(recipeRequest.servingAmount());
            mealEntryRecipe.setCreatedAt(now);
            mealEntryRecipe.setUpdatedAt(now);
            MealEntryRecipeEntity savedLine = mealEntryRecipeRepository.save(mealEntryRecipe);

            for (MealEntryRecipeAdjustmentRequestDto adjustmentRequest : recipeRequest.adjustmentsOrEmpty()) {
                saveAdjustment(savedLine, recipe.getId(), adjustmentRequest, now);
            }
        }

        return getById(savedEntry.getId());
    }

    @Transactional
    public void delete(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        if (!mealEntryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal entry not found with id: " + id);
        }
        mealEntryRepository.deleteById(id);
    }

    private List<MealEntryRecipeRequestDto> resolveRecipeRequests(List<MealEntryRecipeRequestDto> recipes,
                                                                  List<Integer> dishRecipeIds) {
        if (recipes == null || recipes.isEmpty()) {
            return dishRecipeIds.stream()
                    .map(recipeId -> new MealEntryRecipeRequestDto(recipeId, DEFAULT_SERVING_AMOUNT, List.of()))
                    .toList();
        }
        return recipes;
    }

    private void validateRecipeBelongsToDish(Integer dishId, Integer recipeId) {
        if (!dishRecipeRepository.existsByDish_IdAndRecipe_Id(dishId, recipeId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe is not linked to the selected dish. recipe id: " + recipeId);
        }
    }

    private void saveAdjustment(MealEntryRecipeEntity mealEntryRecipe,
                                Integer recipeId,
                                MealEntryRecipeAdjustmentRequestDto request,
                                LocalDateTime timestamp) {
        MealEntryRecipeAdjustmentEntity entity = new MealEntryRecipeAdjustmentEntity();
        entity.setMealEntryRecipe(mealEntryRecipe);
        entity.setAdjustmentType(request.adjustmentType());
        entity.setCreatedAt(timestamp);
        entity.setUpdatedAt(timestamp);

        if (request.adjustmentType() == MealEntryRecipeAdjustmentType.REMOVE) {
            RecipeComponentEntity component = recipeComponentRepository.findByIdAndRecipeId(
                            request.recipeComponentId(), recipeId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Recipe component not found for recipe id: "
                                    + recipeId + " and component id: " + request.recipeComponentId()));
            entity.setRecipeComponent(component);
            mealEntryRecipeAdjustmentRepository.save(entity);
            return;
        }

        applyAddAdjustment(entity, recipeId, request);
        mealEntryRecipeAdjustmentRepository.save(entity);
    }

    private void applyAddAdjustment(MealEntryRecipeAdjustmentEntity entity,
                                    Integer recipeId,
                                    MealEntryRecipeAdjustmentRequestDto request) {
        entity.setComponentType(request.componentType());
        entity.setQuantity(request.quantity());
        entity.setUnit(request.unit().strip());

        if (request.componentType() == RecipeComponentType.INGREDIENT) {
            validateIngredientUnit(request.ingredientId(), entity.getUnit());
            IngredientEntity ingredient = findIngredientById(request.ingredientId());
            entity.setIngredient(ingredient);
            entity.setChildRecipe(null);
            return;
        }

        validateNestedRecipeUnit(entity.getUnit());
        RecipeEntity childRecipe = findRecipeById(request.childRecipeId());
        if (Objects.equals(recipeId, childRecipe.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe cannot reference itself as child recipe. recipe id: " + recipeId);
        }
        entity.setChildRecipe(childRecipe);
        entity.setIngredient(null);
    }

    private void validateIngredientUnit(Integer ingredientId, String unit) {
        if (unit == null || unit.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit is required for ADD adjustments");
        }
        if (isMassOrVolumeUnit(unit)) {
            return;
        }
        if ("batch".equalsIgnoreCase(unit)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unit \"batch\" is only valid for nested recipe ADD adjustments");
        }
        if (!ingredientUnitRepository.existsByIngredientIdAndUnitNormalized(ingredientId, unit)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No grams-per-unit conversion for ingredient id " + ingredientId + " and unit \"" + unit + "\"");
        }
    }

    private static void validateNestedRecipeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit is required for ADD adjustments");
        }
        if (isMassOrVolumeUnit(unit) || "batch".equalsIgnoreCase(unit)) {
            return;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported unit for nested recipe ADD adjustment: \"" + unit + "\" (expected g, ml, or batch)");
    }

    private static boolean isMassOrVolumeUnit(String unit) {
        return "g".equalsIgnoreCase(unit) || "ml".equalsIgnoreCase(unit);
    }

    private DishEntity findDishById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish not found with id: " + id));
    }

    private RecipeEntity findRecipeById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found with id: " + id));
    }

    private IngredientEntity findIngredientById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id));
    }

    private List<MealEntryRecipe> loadRecipesWithAdjustments(Integer mealEntryId) {
        List<MealEntryRecipeEntity> recipeEntities =
                mealEntryRecipeRepository.findByMealEntryIdWithRecipeOrderByIdAsc(mealEntryId);
        Map<Integer, List<MealEntryRecipeAdjustment>> adjustmentsByRecipeLineId =
                loadAdjustmentsByMealEntryRecipeLineId(recipeEntities);

        return recipeEntities.stream()
                .map(recipeEntity -> mealEntryRecipeMapper.toDomain(
                        recipeEntity,
                        adjustmentsByRecipeLineId.getOrDefault(recipeEntity.getId(), List.of())))
                .toList();
    }

    private Map<Integer, List<MealEntryRecipeAdjustment>> loadAdjustmentsByMealEntryRecipeLineId(
            List<MealEntryRecipeEntity> recipeEntities) {
        if (recipeEntities.isEmpty()) {
            return Map.of();
        }

        List<Integer> mealEntryRecipeIds = recipeEntities.stream()
                .map(MealEntryRecipeEntity::getId)
                .toList();

        return mealEntryRecipeAdjustmentRepository
                .findByMealEntryRecipeIdInOrderByMealEntryRecipeIdAscIdAsc(mealEntryRecipeIds)
                .stream()
                .collect(Collectors.groupingBy(
                        adjustment -> adjustment.getMealEntryRecipe().getId(),
                        Collectors.mapping(
                                mealEntryRecipeAdjustmentMapper::toDomain,
                                Collectors.toList())));
    }

    private MealEntryEntity findEntityById(Integer id) {
        return mealEntryRepository.findByIdWithDish(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meal entry not found with id: " + id));
    }
}
