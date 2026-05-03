package com.aleitox.demo.repository;

import com.aleitox.demo.entity.DishRecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRecipeRepository extends JpaRepository<DishRecipeEntity, Integer> {

    @Query("SELECT dr.recipe.id FROM DishRecipeEntity dr WHERE dr.dish.id = :dishId ORDER BY dr.id")
    List<Integer> findRecipeIdsByDishIdOrderByLinkIdAsc(@Param("dishId") Integer dishId);

    void deleteByDish_Id(Integer dishId);
}
