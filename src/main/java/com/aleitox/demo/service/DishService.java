package com.aleitox.demo.service;

import com.aleitox.demo.domain.Dish;
import com.aleitox.demo.dto.DishDetailResponseDto;
import com.aleitox.demo.dto.DishRequestDto;
import com.aleitox.demo.dto.DishResponseDto;
import com.aleitox.demo.entity.DishEntity;
import com.aleitox.demo.mapper.DishMapper;
import com.aleitox.demo.repository.DishRecipeRepository;
import com.aleitox.demo.repository.DishRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DishService {

    private final DishRepository dishRepository;
    private final DishRecipeRepository dishRecipeRepository;
    private final RecipeService recipeService;
    private final DishMapper dishMapper;

    public DishService(DishRepository dishRepository,
                       DishRecipeRepository dishRecipeRepository,
                       RecipeService recipeService,
                       DishMapper dishMapper) {
        this.dishRepository = dishRepository;
        this.dishRecipeRepository = dishRecipeRepository;
        this.recipeService = recipeService;
        this.dishMapper = dishMapper;
    }

    public DishResponseDto create(DishRequestDto request) {
        Dish dish = dishMapper.toDomain(request, null);
        DishEntity entityToSave = dishMapper.toEntity(dish);
        LocalDateTime now = LocalDateTime.now();
        entityToSave.setCreatedAt(now);
        entityToSave.setUpdatedAt(now);

        DishEntity saved = dishRepository.save(entityToSave);
        return dishMapper.toResponseDto(dishMapper.toDomain(saved));
    }

    public DishDetailResponseDto getById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        DishEntity entity = findEntityById(id);
        var dish = dishMapper.toDomain(entity);
        var recipes = dishRecipeRepository.findRecipeIdsByDishIdOrderByLinkIdAsc(id).stream()
                .map(recipeService::getById)
                .toList();
        return dishMapper.toDetailResponseDto(dish, recipes);
    }

    public List<DishResponseDto> getAll() {
        return dishRepository.findAll()
                .stream()
                .map(dishMapper::toDomain)
                .map(dishMapper::toResponseDto)
                .toList();
    }

    public DishResponseDto update(Integer id, DishRequestDto request) {
        Objects.requireNonNull(id, "Id cannot be null");
        DishEntity existing = findEntityById(id);
        Dish incoming = dishMapper.toDomain(request, id);
        existing.setName(incoming.name());
        existing.setDescription(incoming.description());
        existing.setUpdatedAt(LocalDateTime.now());

        DishEntity updated = dishRepository.save(existing);
        return dishMapper.toResponseDto(dishMapper.toDomain(updated));
    }

    public void delete(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        if (!dishRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish not found with id: " + id);
        }
        dishRecipeRepository.deleteByDish_Id(id);
        dishRepository.deleteById(id);
    }

    private DishEntity findEntityById(Integer id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish not found with id: " + id));
    }
}
