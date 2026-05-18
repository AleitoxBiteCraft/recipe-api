package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.MealEntryRecipeAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MealEntryRecipeAdjustmentRepository extends JpaRepository<MealEntryRecipeAdjustmentEntity, Integer> {

    @Query("""
            SELECT mera FROM MealEntryRecipeAdjustmentEntity mera
            LEFT JOIN FETCH mera.recipeComponent
            LEFT JOIN FETCH mera.ingredient
            LEFT JOIN FETCH mera.childRecipe
            WHERE mera.mealEntryRecipe.id IN :mealEntryRecipeIds
            ORDER BY mera.mealEntryRecipe.id, mera.id
            """)
    List<MealEntryRecipeAdjustmentEntity> findByMealEntryRecipeIdInOrderByMealEntryRecipeIdAscIdAsc(
            @Param("mealEntryRecipeIds") Collection<Integer> mealEntryRecipeIds);
}
