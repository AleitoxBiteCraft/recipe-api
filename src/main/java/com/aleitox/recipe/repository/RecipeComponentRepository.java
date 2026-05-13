package com.aleitox.recipe.repository;

import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeComponentRepository extends JpaRepository<RecipeComponentEntity, Integer> {
    List<RecipeComponentEntity> findByRecipeId(Integer recipeId);

    Optional<RecipeComponentEntity> findByIdAndRecipeId(Integer id, Integer recipeId);

    boolean existsByRecipeIdAndComponentTypeAndIngredientId(Integer recipeId, RecipeComponentType componentType, Integer ingredientId);

    boolean existsByRecipeIdAndComponentTypeAndChildRecipeId(Integer recipeId, RecipeComponentType componentType, Integer childRecipeId);

    List<RecipeComponentEntity> findByChildRecipeId(Integer childRecipeId);

    void deleteByRecipeId(Integer recipeId);
}
