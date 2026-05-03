package com.aleitox.demo.mapper;

import com.aleitox.demo.domain.Dish;
import com.aleitox.demo.dto.DishDetailResponseDto;
import com.aleitox.demo.dto.DishRequestDto;
import com.aleitox.demo.dto.DishResponseDto;
import com.aleitox.demo.dto.RecipeResponseDto;
import com.aleitox.demo.entity.DishEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DishMapper {

    public Dish toDomain(DishEntity entity) {
        return new Dish(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public DishEntity toEntity(Dish domain) {
        DishEntity entity = new DishEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }

    public Dish toDomain(DishRequestDto request, Integer id) {
        return new Dish(
                id,
                request.name(),
                request.description(),
                null,
                null
        );
    }

    public DishResponseDto toResponseDto(Dish domain) {
        return new DishResponseDto(
                domain.id(),
                domain.name(),
                domain.description(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }

    public DishDetailResponseDto toDetailResponseDto(Dish domain, List<RecipeResponseDto> recipes) {
        return new DishDetailResponseDto(
                domain.id(),
                domain.name(),
                domain.description(),
                recipes,
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
