package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.domain.Tag;
import com.aleitox.recipe.dto.DishDetailRecipeResponseDto;
import com.aleitox.recipe.dto.RecipeComponentRequestDto;
import com.aleitox.recipe.dto.RecipeComponentResponseDto;
import com.aleitox.recipe.dto.RecipeRequestDto;
import com.aleitox.recipe.dto.RecipeResponseDto;
import com.aleitox.recipe.dto.RecipeStepRequestDto;
import com.aleitox.recipe.dto.RecipeStepResponseDto;
import com.aleitox.recipe.dto.TagResponseDto;
import com.aleitox.recipe.entity.IngredientEntity;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import com.aleitox.recipe.entity.RecipeEntity;
import com.aleitox.recipe.entity.RecipeStepEntity;
import com.aleitox.recipe.mapper.RecipeComponentMapper;
import com.aleitox.recipe.mapper.RecipeMapper;
import com.aleitox.recipe.mapper.RecipeStepMapper;
import com.aleitox.recipe.mapper.TagMapper;
import com.aleitox.recipe.repository.IngredientRepository;
import com.aleitox.recipe.repository.RecipeComponentRepository;
import com.aleitox.recipe.repository.RecipeRepository;
import com.aleitox.recipe.repository.RecipeStepRepository;
import com.aleitox.recipe.repository.RecipeTagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int NUTRITION_SCALE = 4;
    private static final int RESPONSE_NUTRITION_DECIMALS = 2;
    private static final RoundingMode NUTRITION_ROUNDING = RoundingMode.HALF_UP;

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeComponentMapper recipeComponentMapper;
    private final RecipeStepMapper recipeStepMapper;
    private final TagMapper tagMapper;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientRepository ingredientRepository,
                         RecipeComponentRepository recipeComponentRepository,
                         RecipeStepRepository recipeStepRepository,
                         RecipeTagRepository recipeTagRepository,
                         RecipeMapper recipeMapper,
                         RecipeComponentMapper recipeComponentMapper,
                         RecipeStepMapper recipeStepMapper,
                         TagMapper tagMapper) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeComponentRepository = recipeComponentRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeTagRepository = recipeTagRepository;
        this.recipeMapper = recipeMapper;
        this.recipeComponentMapper = recipeComponentMapper;
        this.recipeStepMapper = recipeStepMapper;
        this.tagMapper = tagMapper;
    }

    @Transactional
    public RecipeResponseDto create(RecipeRequestDto request) {
        validateDuplicateComponents(request.components());
        validateDuplicateStepOrders(request.steps());

        LocalDateTime now = LocalDateTime.now();
        RecipeEntity recipe = recipeMapper.toEntity(recipeMapper.toDomain(request, null));
        recipe.setCreatedAt(now);
        recipe.setUpdatedAt(now);
        RecipeEntity savedRecipe = recipeRepository.save(recipe);

        List<RecipeComponentEntity> savedComponents = saveComponents(savedRecipe, request.components(), now);
        List<RecipeStepEntity> savedSteps = saveSteps(savedRecipe, request.steps(), now);
        return toResponse(savedRecipe, savedComponents, savedSteps);
    }

    public List<RecipeResponseDto> getAll() {
        return recipeRepository.findAll()
                .stream()
                .map(recipe -> toResponse(
                        recipe,
                        recipeComponentRepository.findByRecipeId(recipe.getId()),
                        recipeStepRepository.findByRecipeIdOrderByStepOrderAsc(recipe.getId())
                ))
                .toList();
    }

    public RecipeResponseDto getById(Integer id) {
        RecipeEntity recipe = findRecipeById(id);
        return toResponse(
                recipe,
                recipeComponentRepository.findByRecipeId(id),
                recipeStepRepository.findByRecipeIdOrderByStepOrderAsc(id)
        );
    }

    @Transactional(readOnly = true)
    public DishDetailRecipeResponseDto toDishDetailRecipeTree(Integer recipeId) {
        Objects.requireNonNull(recipeId, "Id cannot be null");
        return buildDishDetailRecipeTree(recipeId, new HashSet<>());
    }

    private DishDetailRecipeResponseDto buildDishDetailRecipeTree(Integer recipeId, Set<Integer> expandedRecipeIds) {
        RecipeEntity recipe = findRecipeById(recipeId);
        if (!expandedRecipeIds.add(recipeId)) {
            return new DishDetailRecipeResponseDto(
                    recipe.getId(),
                    recipe.getName(),
                    recipe.getDescription(),
                    recipe.getServing(),
                    List.of()
            );
        }
        List<RecipeComponentEntity> components = recipeComponentRepository.findByRecipeId(recipeId);
        List<DishDetailRecipeResponseDto> subRecipes = new ArrayList<>();
        for (RecipeComponentEntity component : components) {
            if (component.getComponentType() != RecipeComponentType.RECIPE || component.getChildRecipe() == null) {
                continue;
            }
            Integer childRecipeId = component.getChildRecipe().getId();
            subRecipes.add(buildDishDetailRecipeTree(childRecipeId, expandedRecipeIds));
        }
        return new DishDetailRecipeResponseDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getServing(),
                List.copyOf(subRecipes)
        );
    }

    @Transactional
    public RecipeResponseDto update(Integer id, RecipeRequestDto request) {
        validateDuplicateComponents(request.components());
        validateDuplicateStepOrders(request.steps());

        RecipeEntity existing = findRecipeById(id);
        var incoming = recipeMapper.toDomain(request, id);
        existing.setName(incoming.name());
        existing.setDescription(incoming.description());
        existing.setServing(incoming.serving());
        existing.setUpdatedAt(LocalDateTime.now());
        RecipeEntity updatedRecipe = recipeRepository.save(existing);

        recipeComponentRepository.deleteByRecipeId(id);
        recipeStepRepository.deleteByRecipeId(id);

        LocalDateTime now = LocalDateTime.now();
        List<RecipeComponentEntity> savedComponents = saveComponents(updatedRecipe, request.components(), now);
        List<RecipeStepEntity> savedSteps = saveSteps(updatedRecipe, request.steps(), now);
        return toResponse(updatedRecipe, savedComponents, savedSteps);
    }

    @Transactional
    public void delete(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        if (!recipeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found with id: " + id);
        }
        recipeComponentRepository.deleteByRecipeId(id);
        recipeStepRepository.deleteByRecipeId(id);
        recipeTagRepository.deleteByRecipe_Id(id);
        recipeRepository.deleteById(id);
    }

    @Transactional
    public RecipeComponentResponseDto addComponent(Integer recipeId, RecipeComponentRequestDto request) {
        RecipeEntity recipe = findRecipeById(recipeId);
        validateRecipeComponentDoesNotExist(recipeId, request);

        RecipeComponentEntity entity = buildRecipeComponentEntity(recipe, request, null);
        RecipeComponentEntity saved = saveRecipeComponent(entity);
        touchRecipeUpdateTime(recipe);
        return recipeComponentMapper.toResponseDto(recipeComponentMapper.toDomain(saved));
    }

    @Transactional
    public RecipeComponentResponseDto updateComponent(Integer recipeId,
                                                      Integer recipeComponentId,
                                                      RecipeComponentRequestDto request) {
        RecipeEntity recipe = findRecipeById(recipeId);
        RecipeComponentEntity existing = recipeComponentRepository.findByIdAndRecipeId(recipeComponentId, recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe component not found with id: " + recipeComponentId + " for recipe id: " + recipeId));

        validateRecipeComponentDoesNotExist(recipeId, request, recipeComponentId);
        applyRecipeComponentRequest(existing, request, recipe.getId());
        existing.setUpdatedAt(LocalDateTime.now());

        RecipeComponentEntity saved = recipeComponentRepository.save(existing);
        touchRecipeUpdateTime(recipe);
        return recipeComponentMapper.toResponseDto(recipeComponentMapper.toDomain(saved));
    }

    @Transactional
    public void deleteComponent(Integer recipeId, Integer recipeComponentId) {
        RecipeEntity recipe = findRecipeById(recipeId);
        RecipeComponentEntity existing = recipeComponentRepository.findByIdAndRecipeId(recipeComponentId, recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe component not found with id: " + recipeComponentId + " for recipe id: " + recipeId));
        recipeComponentRepository.deleteById(Objects.requireNonNull(existing.getId(), "Recipe component id cannot be null"));
        touchRecipeUpdateTime(recipe);
    }

    @Transactional
    public RecipeStepResponseDto addStep(Integer recipeId, RecipeStepRequestDto request) {
        RecipeEntity recipe = findRecipeById(recipeId);
        validateStepOrderDoesNotExist(recipeId, request.stepOrder());

        RecipeStepEntity entity = new RecipeStepEntity();
        entity.setRecipe(recipe);
        entity.setStepOrder(request.stepOrder());
        entity.setDescription(request.description());
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        RecipeStepEntity saved = recipeStepRepository.save(entity);
        touchRecipeUpdateTime(recipe);
        return recipeStepMapper.toResponseDto(recipeStepMapper.toDomain(saved));
    }

    @Transactional
    public RecipeStepResponseDto updateStep(Integer recipeId, Integer recipeStepId, RecipeStepRequestDto request) {
        RecipeEntity recipe = findRecipeById(recipeId);
        RecipeStepEntity existing = recipeStepRepository.findByIdAndRecipeId(recipeStepId, recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe step not found with id: " + recipeStepId + " for recipe id: " + recipeId));

        if (!Objects.equals(existing.getStepOrder(), request.stepOrder())
                && recipeStepRepository.existsByRecipeIdAndStepOrder(recipeId, request.stepOrder())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe step order already exists for recipe id: " + recipeId + " and step order: " + request.stepOrder());
        }

        existing.setStepOrder(request.stepOrder());
        existing.setDescription(request.description());
        existing.setUpdatedAt(LocalDateTime.now());

        RecipeStepEntity saved = recipeStepRepository.save(existing);
        touchRecipeUpdateTime(recipe);
        return recipeStepMapper.toResponseDto(recipeStepMapper.toDomain(saved));
    }

    @Transactional
    public void deleteStep(Integer recipeId, Integer recipeStepId) {
        RecipeEntity recipe = findRecipeById(recipeId);
        RecipeStepEntity existing = recipeStepRepository.findByIdAndRecipeId(recipeStepId, recipeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipe step not found with id: " + recipeStepId + " for recipe id: " + recipeId));
        recipeStepRepository.deleteById(Objects.requireNonNull(existing.getId(), "Recipe step id cannot be null"));
        touchRecipeUpdateTime(recipe);
    }

    private RecipeEntity findRecipeById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found with id: " + id));
    }

    private IngredientEntity findIngredientById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id));
    }

    private List<RecipeComponentEntity> saveComponents(RecipeEntity recipe,
                                                       List<RecipeComponentRequestDto> components,
                                                       LocalDateTime timestamp) {
        List<RecipeComponentEntity> savedComponents = new java.util.ArrayList<>();
        for (RecipeComponentRequestDto request : components) {
            RecipeComponentEntity entity = buildRecipeComponentEntity(recipe, request, timestamp);
            savedComponents.add(saveRecipeComponent(entity));
        }
        return savedComponents;
    }

    @SuppressWarnings({"null", "ConstantConditions"})
    private RecipeComponentEntity saveRecipeComponent(RecipeComponentEntity entity) {
        return recipeComponentRepository.save(entity);
    }

    private List<RecipeStepEntity> saveSteps(RecipeEntity recipe,
                                             List<RecipeStepRequestDto> steps,
                                             LocalDateTime timestamp) {
        return steps.stream()
                .map(request -> {
                    RecipeStepEntity entity = new RecipeStepEntity();
                    entity.setRecipe(recipe);
                    entity.setStepOrder(request.stepOrder());
                    entity.setDescription(request.description());
                    entity.setCreatedAt(timestamp);
                    entity.setUpdatedAt(timestamp);
                    return recipeStepRepository.save(entity);
                })
                .toList();
    }

    private void validateDuplicateComponents(List<RecipeComponentRequestDto> components) {
        Set<String> componentKeys = new HashSet<>();
        for (RecipeComponentRequestDto component : components) {
            String referenceKey = component.componentType() == RecipeComponentType.INGREDIENT
                    ? String.valueOf(component.ingredientId())
                    : String.valueOf(component.childRecipeId());
            String key = component.componentType() + ":" + referenceKey;
            if (!componentKeys.add(key)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate component in recipe components: " + key);
            }
        }
    }

    private void validateDuplicateStepOrders(List<RecipeStepRequestDto> steps) {
        Set<Integer> stepOrders = new HashSet<>();
        for (RecipeStepRequestDto step : steps) {
            if (!stepOrders.add(step.stepOrder())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate step order in recipe steps: " + step.stepOrder());
            }
        }
    }

    private void validateRecipeComponentDoesNotExist(Integer recipeId, RecipeComponentRequestDto request) {
        validateRecipeComponentDoesNotExist(recipeId, request, null);
    }

    private void validateRecipeComponentDoesNotExist(Integer recipeId,
                                                     RecipeComponentRequestDto request,
                                                     Integer currentComponentId) {
        RecipeComponentType componentType = request.componentType();
        boolean exists = componentType == RecipeComponentType.INGREDIENT
                ? recipeComponentRepository.existsByRecipeIdAndComponentTypeAndIngredientId(recipeId, componentType, request.ingredientId())
                : recipeComponentRepository.existsByRecipeIdAndComponentTypeAndChildRecipeId(recipeId, componentType, request.childRecipeId());

        if (!exists) {
            return;
        }

        if (currentComponentId != null) {
            RecipeComponentEntity existing = recipeComponentRepository.findByIdAndRecipeId(currentComponentId, recipeId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Recipe component not found with id: " + currentComponentId + " for recipe id: " + recipeId));
            boolean sameReference = existing.getComponentType() == componentType
                    && ((componentType == RecipeComponentType.INGREDIENT
                    && Objects.equals(existing.getIngredient().getId(), request.ingredientId()))
                    || (componentType == RecipeComponentType.RECIPE
                    && Objects.equals(existing.getChildRecipe().getId(), request.childRecipeId())));
            if (sameReference) {
                return;
            }
        }

        if (componentType == RecipeComponentType.INGREDIENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ingredient already linked to recipe. recipe id: " + recipeId + ", ingredient id: " + request.ingredientId());
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Child recipe already linked to recipe. recipe id: " + recipeId + ", child recipe id: " + request.childRecipeId());
    }

    private RecipeComponentEntity buildRecipeComponentEntity(RecipeEntity recipe,
                                                             RecipeComponentRequestDto request,
                                                             LocalDateTime timestamp) {
        RecipeComponentEntity entity = new RecipeComponentEntity();
        entity.setRecipe(recipe);
        applyRecipeComponentRequest(entity, request, recipe.getId());
        LocalDateTime now = timestamp != null ? timestamp : LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    private void applyRecipeComponentRequest(RecipeComponentEntity entity,
                                             RecipeComponentRequestDto request,
                                             Integer parentRecipeId) {
        entity.setComponentType(request.componentType());
        entity.setQuantity(request.quantity());
        entity.setUnit(request.unit());

        if (request.componentType() == RecipeComponentType.INGREDIENT) {
            IngredientEntity ingredient = findIngredientById(request.ingredientId());
            entity.setIngredient(ingredient);
            entity.setChildRecipe(null);
            return;
        }

        RecipeEntity childRecipe = findRecipeById(request.childRecipeId());
        validateChildRecipeReference(parentRecipeId, childRecipe.getId());
        entity.setChildRecipe(childRecipe);
        entity.setIngredient(null);
    }

    private void validateChildRecipeReference(Integer parentRecipeId, Integer childRecipeId) {
        if (Objects.equals(parentRecipeId, childRecipeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recipe cannot reference itself as child recipe");
        }
        if (isRecipeReachableFrom(childRecipeId, parentRecipeId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cycle detected in recipe composition. parent recipe id: " + parentRecipeId + ", child recipe id: " + childRecipeId);
        }
    }

    private boolean isRecipeReachableFrom(Integer startRecipeId, Integer targetRecipeId) {
        Queue<Integer> queue = new ArrayDeque<>();
        Set<Integer> visited = new HashSet<>();
        queue.add(startRecipeId);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            if (Objects.equals(current, targetRecipeId)) {
                return true;
            }
            List<RecipeComponentEntity> children = recipeComponentRepository.findByRecipeId(current);
            for (RecipeComponentEntity component : children) {
                if (component.getComponentType() == RecipeComponentType.RECIPE && component.getChildRecipe() != null) {
                    queue.add(component.getChildRecipe().getId());
                }
            }
        }
        return false;
    }

    private void validateStepOrderDoesNotExist(Integer recipeId, Integer stepOrder) {
        if (recipeStepRepository.existsByRecipeIdAndStepOrder(recipeId, stepOrder)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipe step order already exists for recipe id: " + recipeId + " and step order: " + stepOrder);
        }
    }

    private void touchRecipeUpdateTime(RecipeEntity recipe) {
        recipe.setUpdatedAt(LocalDateTime.now());
        recipeRepository.save(recipe);
    }

    private RecipeResponseDto toResponse(RecipeEntity recipe,
                                         List<RecipeComponentEntity> components,
                                         List<RecipeStepEntity> steps) {
        BatchTotals batchTotals = computeBatchTotals(recipe.getId(), new HashSet<>());
        Set<TagResponseDto> tags = computeEffectiveTags(recipe.getId(), new HashSet<>());
        return new RecipeResponseDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getServing(),
                roundForResponse(batchTotals.calories()),
                roundForResponse(batchTotals.proteins()),
                roundForResponse(batchTotals.carbohydrates()),
                roundForResponse(batchTotals.fats()),
                components.stream()
                        .map(recipeComponentMapper::toDomain)
                        .map(recipeComponentMapper::toResponseDto)
                        .toList(),
                steps.stream()
                        .map(recipeStepMapper::toDomain)
                        .map(recipeStepMapper::toResponseDto)
                        .toList(),
                tags,
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    /**
     * Tags linked to the recipe plus tags from nested {@link RecipeComponentType#RECIPE} components
     * (transitive). Duplicates are merged by tag id. Iteration order is by name (case-insensitive).
     */
    private Set<TagResponseDto> computeEffectiveTags(Integer recipeId, Set<Integer> visiting) {
        if (!visiting.add(recipeId)) {
            return Set.of();
        }
        try {
            Map<Integer, Tag> byId = new HashMap<>();
            for (var link : recipeTagRepository.findWithTagByRecipeId(recipeId)) {
                Tag tag = tagMapper.toDomain(link.getTag());
                byId.putIfAbsent(tag.id(), tag);
            }
            for (RecipeComponentEntity component : recipeComponentRepository.findByRecipeId(recipeId)) {
                if (component.getComponentType() != RecipeComponentType.RECIPE || component.getChildRecipe() == null) {
                    continue;
                }
                for (TagResponseDto inherited : computeEffectiveTags(component.getChildRecipe().getId(), visiting)) {
                    Tag tag = new Tag(
                            inherited.id(),
                            inherited.name(),
                            inherited.createdAt(),
                            inherited.updatedAt());
                    byId.putIfAbsent(tag.id(), tag);
                }
            }
            return byId.values().stream()
                    .sorted(Comparator.comparing(Tag::name, String.CASE_INSENSITIVE_ORDER))
                    .map(tagMapper::toResponseDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } finally {
            visiting.remove(recipeId);
        }
    }

    /**
     * Totals for one full recipe "batch" (all components scaled by quantity and unit).
     * Mass in grams is the summed mass of components (g, ml treated as g; nested {@code batch} uses child batch mass).
     */
    private BatchTotals computeBatchTotals(Integer recipeId, Set<Integer> visiting) {
        if (!visiting.add(recipeId)) {
            return BatchTotals.zero();
        }
        try {
            BatchTotals acc = BatchTotals.zero();
            for (RecipeComponentEntity component : recipeComponentRepository.findByRecipeId(recipeId)) {
                acc = acc.add(componentTotals(component, visiting));
            }
            return acc;
        } finally {
            visiting.remove(recipeId);
        }
    }

    private BatchTotals componentTotals(RecipeComponentEntity component, Set<Integer> visiting) {
        if (component.getComponentType() == RecipeComponentType.INGREDIENT) {
            if (component.getIngredient() == null) {
                return BatchTotals.zero();
            }
            BigDecimal grams = ingredientGrams(component.getQuantity(), component.getUnit());
            IngredientEntity ing = component.getIngredient();
            return new BatchTotals(
                    scalePer100g(ing.getCaloriesPer100g(), grams),
                    scalePer100g(ing.getProteinsPer100g(), grams),
                    scalePer100g(ing.getCarbohydratesPer100g(), grams),
                    scalePer100g(ing.getFatsPer100g(), grams),
                    grams
            );
        }
        if (component.getComponentType() == RecipeComponentType.RECIPE && component.getChildRecipe() != null) {
            Integer childId = component.getChildRecipe().getId();
            BatchTotals childBatch = computeBatchTotals(childId, visiting);
            String unit = component.getUnit();
            BigDecimal qty = component.getQuantity();
            if (unit == null) {
                return BatchTotals.zero();
            }
            if ("batch".equalsIgnoreCase(unit.strip())) {
                return childBatch.scale(qty);
            }
            BigDecimal grams = ingredientGrams(qty, unit);
            return childBatch.scaleToMass(grams);
        }
        return BatchTotals.zero();
    }

    private static BigDecimal ingredientGrams(BigDecimal quantity, String unit) {
        if (quantity == null || unit == null) {
            return BigDecimal.ZERO;
        }
        String u = unit.strip();
        if ("g".equalsIgnoreCase(u) || "ml".equalsIgnoreCase(u)) {
            return quantity;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported recipe component unit for nutrition: \"" + unit + "\" (expected g, ml, or batch for nested recipes)");
    }

    private static BigDecimal scalePer100g(BigDecimal per100g, BigDecimal grams) {
        if (per100g == null || grams.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return per100g.multiply(grams).divide(HUNDRED, NUTRITION_SCALE, NUTRITION_ROUNDING);
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

        BatchTotals add(BatchTotals o) {
            return new BatchTotals(
                    calories.add(o.calories),
                    proteins.add(o.proteins),
                    carbohydrates.add(o.carbohydrates),
                    fats.add(o.fats),
                    massGrams.add(o.massGrams)
            );
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
                    massGrams.multiply(factor)
            );
        }

        /**
         * Use a portion of {@code grams} from a batch with totals {@code this} and mass {@link #massGrams()}.
         */
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
                    grams
            );
        }
    }
}
