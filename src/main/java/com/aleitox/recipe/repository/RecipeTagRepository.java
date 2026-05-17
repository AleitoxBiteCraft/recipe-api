package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.RecipeTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeTagRepository extends JpaRepository<RecipeTagEntity, Integer> {

    @Query("SELECT rt FROM RecipeTagEntity rt JOIN FETCH rt.tag WHERE rt.recipe.id = :recipeId")
    List<RecipeTagEntity> findWithTagByRecipeId(@Param("recipeId") Integer recipeId);

    void deleteByRecipe_Id(Integer recipeId);
}
