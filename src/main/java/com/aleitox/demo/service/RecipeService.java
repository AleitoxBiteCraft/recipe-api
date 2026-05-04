package com.aleitox.demo.service;

import com.aleitox.demo.domain.RecipeComponentType;
import com.aleitox.demo.dto.DishDetailRecipeResponseDto;
import com.aleitox.demo.dto.RecipeComponentRequestDto;
import com.aleitox.demo.dto.RecipeComponentResponseDto;
import com.aleitox.demo.dto.RecipeRequestDto;
import com.aleitox.demo.dto.RecipeResponseDto;
import com.aleitox.demo.dto.RecipeStepRequestDto;
import com.aleitox.demo.dto.RecipeStepResponseDto;
import com.aleitox.demo.entity.IngredientEntity;
import com.aleitox.demo.entity.RecipeComponentEntity;
import com.aleitox.demo.entity.RecipeEntity;
import com.aleitox.demo.entity.RecipeStepEntity;
import com.aleitox.demo.mapper.RecipeComponentMapper;
import com.aleitox.demo.mapper.RecipeMapper;
import com.aleitox.demo.mapper.RecipeStepMapper;
import com.aleitox.demo.repository.IngredientRepository;
import com.aleitox.demo.repository.RecipeComponentRepository;
import com.aleitox.demo.repository.RecipeRepository;
import com.aleitox.demo.repository.RecipeStepRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeMapper recipeMapper;
    private final RecipeComponentMapper recipeComponentMapper;
    private final RecipeStepMapper recipeStepMapper;

    public RecipeService(RecipeRepository recipeRepository,
                         IngredientRepository ingredientRepository,
                         RecipeComponentRepository recipeComponentRepository,
                         RecipeStepRepository recipeStepRepository,
                         RecipeMapper recipeMapper,
                         RecipeComponentMapper recipeComponentMapper,
                         RecipeStepMapper recipeStepMapper) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeComponentRepository = recipeComponentRepository;
        this.recipeStepRepository = recipeStepRepository;
        this.recipeMapper = recipeMapper;
        this.recipeComponentMapper = recipeComponentMapper;
        this.recipeStepMapper = recipeStepMapper;
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
        return new RecipeResponseDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                components.stream()
                        .map(recipeComponentMapper::toDomain)
                        .map(recipeComponentMapper::toResponseDto)
                        .toList(),
                steps.stream()
                        .map(recipeStepMapper::toDomain)
                        .map(recipeStepMapper::toResponseDto)
                        .toList(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
}
