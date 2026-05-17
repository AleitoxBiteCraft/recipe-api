package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.Ingredient;
import com.aleitox.recipe.domain.IngredientUnit;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.IngredientResponseDto;
import com.aleitox.recipe.dto.IngredientUnitRequestDto;
import com.aleitox.recipe.entity.IngredientEntity;
import com.aleitox.recipe.entity.IngredientUnitEntity;
import com.aleitox.recipe.mapper.IngredientMapper;
import com.aleitox.recipe.mapper.IngredientUnitMapper;
import com.aleitox.recipe.repository.IngredientRepository;
import com.aleitox.recipe.repository.IngredientUnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientUnitRepository ingredientUnitRepository;
    private final IngredientMapper ingredientMapper;
    private final IngredientUnitMapper ingredientUnitMapper;

    public IngredientService(IngredientRepository ingredientRepository,
                             IngredientUnitRepository ingredientUnitRepository,
                             IngredientMapper ingredientMapper,
                             IngredientUnitMapper ingredientUnitMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientUnitRepository = ingredientUnitRepository;
        this.ingredientMapper = ingredientMapper;
        this.ingredientUnitMapper = ingredientUnitMapper;
    }

    @Transactional
    public IngredientResponseDto create(IngredientRequestDto request) {
        Ingredient ingredient = ingredientMapper.toDomain(request, null);
        IngredientEntity entityToSave = ingredientMapper.toEntity(ingredient);
        LocalDateTime now = LocalDateTime.now();
        entityToSave.setCreatedAt(now);
        entityToSave.setUpdatedAt(now);

        IngredientEntity saved = ingredientRepository.save(entityToSave);
        List<IngredientUnit> savedUnits = saveUnits(saved, request.units(), now);
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(saved, savedUnits));
    }

    @Transactional(readOnly = true)
    public IngredientResponseDto getById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        IngredientEntity entity = findEntityById(id);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<IngredientResponseDto> getAll() {
        List<IngredientEntity> ingredients = ingredientRepository.findAll();
        Map<Integer, List<IngredientUnit>> unitsByIngredientId = loadUnitsByIngredientId(
                ingredients.stream().map(IngredientEntity::getId).toList());
        return ingredients.stream()
                .map(entity -> ingredientMapper.toResponseDto(
                        ingredientMapper.toDomain(entity, unitsByIngredientId.getOrDefault(entity.getId(), List.of()))))
                .toList();
    }

    @Transactional
    public IngredientResponseDto update(Integer id, IngredientRequestDto request) {
        Objects.requireNonNull(id, "Id cannot be null");
        IngredientEntity existing = findEntityById(id);
        Ingredient incoming = ingredientMapper.toDomain(request, id);
        existing.setName(incoming.name());
        existing.setCaloriesPer100g(incoming.caloriesPer100g());
        existing.setProteinsPer100g(incoming.proteinsPer100g());
        existing.setCarbohydratesPer100g(incoming.carbohydratesPer100g());
        existing.setFatsPer100g(incoming.fatsPer100g());
        existing.setNutritionSource(incoming.nutritionSource());
        LocalDateTime now = LocalDateTime.now();
        existing.setUpdatedAt(now);

        IngredientEntity updated = ingredientRepository.save(existing);
        ingredientUnitRepository.deleteByIngredientId(id);
        List<IngredientUnit> savedUnits = saveUnits(updated, request.units(), now);
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(updated, savedUnits));
    }

    @Transactional
    public void delete(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        if (!ingredientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id);
        }
        ingredientRepository.deleteById(id);
    }

    private IngredientResponseDto toResponse(IngredientEntity entity) {
        List<IngredientUnit> units = loadUnits(entity.getId());
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(entity, units));
    }

    private List<IngredientUnit> loadUnits(Integer ingredientId) {
        return ingredientUnitRepository.findByIngredientIdOrderByUnitAsc(ingredientId)
                .stream()
                .map(ingredientUnitMapper::toDomain)
                .toList();
    }

    private Map<Integer, List<IngredientUnit>> loadUnitsByIngredientId(List<Integer> ingredientIds) {
        if (ingredientIds.isEmpty()) {
            return Map.of();
        }
        return ingredientUnitRepository.findByIngredientIdIn(ingredientIds)
                .stream()
                .map(ingredientUnitMapper::toDomain)
                .collect(Collectors.groupingBy(IngredientUnit::ingredientId));
    }

    private List<IngredientUnit> saveUnits(IngredientEntity ingredient,
                                           List<IngredientUnitRequestDto> unitRequests,
                                           LocalDateTime timestamp) {
        if (unitRequests == null || unitRequests.isEmpty()) {
            return List.of();
        }
        List<IngredientUnit> saved = new ArrayList<>();
        for (IngredientUnitRequestDto unitRequest : unitRequests) {
            IngredientUnitEntity entity = new IngredientUnitEntity();
            entity.setIngredient(ingredient);
            entity.setUnit(unitRequest.unit().strip());
            entity.setGramsPerUnit(unitRequest.gramsPerUnit());
            entity.setCreatedAt(timestamp);
            entity.setUpdatedAt(timestamp);
            IngredientUnitEntity persisted = ingredientUnitRepository.save(entity);
            saved.add(ingredientUnitMapper.toDomain(persisted));
        }
        return saved;
    }

    private IngredientEntity findEntityById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id));
    }
}
