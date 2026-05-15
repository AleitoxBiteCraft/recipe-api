package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.DishRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRecipeRepository extends JpaRepository<DishRecipeEntity, Integer> {

    @Query("SELECT dr.recipe.id FROM DishRecipeEntity dr WHERE dr.dish.id = :dishId ORDER BY dr.id")
    List<Integer> findRecipeIdsByDishIdOrderByLinkIdAsc(@Param("dishId") Integer dishId);

    /**
     * Sum of {@code recipe.serving} for recipes linked to the dish (top level only).
     * SQL {@code SUM} ignores null servings.
     */
    @Query("SELECT COALESCE(SUM(r.serving), 0) FROM DishRecipeEntity dr JOIN dr.recipe r WHERE dr.dish.id = :dishId")
    long sumServingsByDishId(@Param("dishId") Integer dishId);

    void deleteByDish_Id(Integer dishId);
}
