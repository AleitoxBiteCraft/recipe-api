package com.aleitox.recipe.entity;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_entry_recipe_adjustment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryRecipeAdjustmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_entry_recipe_id", nullable = false)
    private MealEntryRecipeEntity mealEntryRecipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 10)
    private MealEntryRecipeAdjustmentType adjustmentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_component_id")
    private RecipeComponentEntity recipeComponent;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", length = 20)
    private RecipeComponentType componentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private IngredientEntity ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_recipe_id")
    private RecipeEntity childRecipe;

    @Column
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
