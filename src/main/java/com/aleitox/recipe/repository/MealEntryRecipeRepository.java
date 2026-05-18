package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealEntryRecipeRepository extends JpaRepository<MealEntryRecipeEntity, Integer> {

    @Query("""
            SELECT mer FROM MealEntryRecipeEntity mer
            JOIN FETCH mer.recipe
            WHERE mer.mealEntry.id = :mealEntryId
            ORDER BY mer.id
            """)
    List<MealEntryRecipeEntity> findByMealEntryIdWithRecipeOrderByIdAsc(@Param("mealEntryId") Integer mealEntryId);
}
