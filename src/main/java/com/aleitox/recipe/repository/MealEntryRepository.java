package com.aleitox.recipe.repository;

import com.aleitox.recipe.entity.MealEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntryEntity, Integer> {

    @Query("SELECT m FROM MealEntryEntity m JOIN FETCH m.dish ORDER BY m.eatenAt DESC")
    List<MealEntryEntity> findAllWithDishOrderByEatenAtDesc();

    @Query("SELECT m FROM MealEntryEntity m JOIN FETCH m.dish WHERE m.id = :id")
    Optional<MealEntryEntity> findByIdWithDish(@Param("id") Integer id);
}
