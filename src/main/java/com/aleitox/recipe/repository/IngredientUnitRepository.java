package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.IngredientUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientUnitRepository extends JpaRepository<IngredientUnitEntity, Integer> {

    List<IngredientUnitEntity> findByIngredientIdOrderByUnitAsc(Integer ingredientId);

    List<IngredientUnitEntity> findByIngredientIdIn(Collection<Integer> ingredientIds);

    void deleteByIngredientId(Integer ingredientId);

    @Query("""
            SELECT iu FROM IngredientUnitEntity iu
            WHERE iu.ingredient.id = :ingredientId
            AND UPPER(TRIM(iu.unit)) = UPPER(TRIM(:unit))
            """)
    Optional<IngredientUnitEntity> findByIngredientIdAndUnitNormalized(
            @Param("ingredientId") Integer ingredientId,
            @Param("unit") String unit);

    @Query("""
            SELECT CASE WHEN COUNT(iu) > 0 THEN true ELSE false END
            FROM IngredientUnitEntity iu
            WHERE iu.ingredient.id = :ingredientId
            AND UPPER(TRIM(iu.unit)) = UPPER(TRIM(:unit))
            """)
    boolean existsByIngredientIdAndUnitNormalized(
            @Param("ingredientId") Integer ingredientId,
            @Param("unit") String unit);
}
