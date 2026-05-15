package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.Recipe;
import com.aleitox.recipe.dto.RecipeRequestDto;
import com.aleitox.recipe.entity.RecipeEntity;
import org.springframework.stereotype.Component;

@Component
public class RecipeMapper {

    public Recipe toDomain(RecipeEntity entity) {
        return new Recipe(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getServing(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public RecipeEntity toEntity(Recipe domain) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setServing(domain.serving());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }

    public Recipe toDomain(RecipeRequestDto request, Integer id) {
        return new Recipe(
                id,
                request.name(),
                request.description(),
                request.serving(),
                null,
                null
        );
    }
}
