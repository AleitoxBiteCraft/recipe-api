package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.RecipeStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStepEntity, Integer> {
    List<RecipeStepEntity> findByRecipeIdOrderByStepOrderAsc(Integer recipeId);

    Optional<RecipeStepEntity> findByIdAndRecipeId(Integer id, Integer recipeId);

    boolean existsByRecipeIdAndStepOrder(Integer recipeId, Integer stepOrder);

    void deleteByRecipeId(Integer recipeId);
}
