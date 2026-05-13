package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.Ingredient;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.IngredientResponseDto;
import com.aleitox.recipe.entity.IngredientEntity;
import com.aleitox.recipe.mapper.IngredientMapper;
import com.aleitox.recipe.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientService(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }

    public IngredientResponseDto create(IngredientRequestDto request) {
        Ingredient ingredient = ingredientMapper.toDomain(request, null);
        IngredientEntity entityToSave = ingredientMapper.toEntity(ingredient);
        LocalDateTime now = LocalDateTime.now();
        entityToSave.setCreatedAt(now);
        entityToSave.setUpdatedAt(now);

        IngredientEntity saved = ingredientRepository.save(entityToSave);
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(saved));
    }

    public IngredientResponseDto getById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        IngredientEntity entity = findEntityById(id);
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(entity));
    }

    public List<IngredientResponseDto> getAll() {
        return ingredientRepository.findAll()
                .stream()
                .map(ingredientMapper::toDomain)
                .map(ingredientMapper::toResponseDto)
                .toList();
    }

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
        existing.setUpdatedAt(LocalDateTime.now());

        IngredientEntity updated = ingredientRepository.save(existing);
        return ingredientMapper.toResponseDto(ingredientMapper.toDomain(updated));
    }

    public void delete(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        if (!ingredientRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id);
        }
        ingredientRepository.deleteById(id);
    }

    private IngredientEntity findEntityById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found with id: " + id));
    }
}
