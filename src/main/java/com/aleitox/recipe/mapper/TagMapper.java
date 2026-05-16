package com.aleitox.recipe.mapper;

import com.aleitox.recipe.domain.Tag;
import com.aleitox.recipe.dto.TagResponseDto;
import com.aleitox.recipe.entity.TagEntity;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag toDomain(TagEntity entity) {
        return new Tag(
                entity.getId(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public TagResponseDto toResponseDto(Tag domain) {
        return new TagResponseDto(
                domain.id(),
                domain.name(),
                domain.createdAt(),
                domain.updatedAt()
        );
    }
}
