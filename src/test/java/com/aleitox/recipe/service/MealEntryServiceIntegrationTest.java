package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.MealEntryRecipeAdjustmentType;
import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.dto.DishRequestDto;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.RecipeComponentRequestDto;
import com.aleitox.recipe.dto.RecipeRequestDto;
import com.aleitox.recipe.entity.DishEntity;
import com.aleitox.recipe.entity.MealEntryEntity;
import com.aleitox.recipe.entity.MealEntryRecipeAdjustmentEntity;
import com.aleitox.recipe.entity.MealEntryRecipeEntity;
import com.aleitox.recipe.entity.RecipeComponentEntity;
import com.aleitox.recipe.entity.RecipeEntity;
import com.aleitox.recipe.repository.DishRepository;
import com.aleitox.recipe.repository.MealEntryRecipeAdjustmentRepository;
import com.aleitox.recipe.repository.MealEntryRecipeRepository;
import com.aleitox.recipe.repository.MealEntryRepository;
import com.aleitox.recipe.repository.RecipeComponentRepository;
import com.aleitox.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MealEntryServiceIntegrationTest {

    @Autowired
    private MealEntryService mealEntryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeComponentRepository recipeComponentRepository;

    @Autowired
    private MealEntryRepository mealEntryRepository;

    @Autowired
    private MealEntryRecipeRepository mealEntryRecipeRepository;

    @Autowired
    private MealEntryRecipeAdjustmentRepository mealEntryRecipeAdjustmentRepository;

    @Test
    void getAll_returnsMealEntriesOrderedByEatenAtDesc() {
        var dish = dishService.create(new DishRequestDto("Lunch plate", null));
        LocalDateTime earlier = LocalDateTime.of(2026, 5, 10, 12, 0);
        LocalDateTime later = LocalDateTime.of(2026, 5, 11, 12, 0);
        saveMealEntry(dish.id(), earlier, "Breakfast");
        saveMealEntry(dish.id(), later, "Lunch");

        var entries = mealEntryService.getAll();

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).eatenAt()).isEqualTo(later);
        assertThat(entries.get(1).eatenAt()).isEqualTo(earlier);
        assertThat(entries.getFirst().dishName()).isEqualTo("Lunch plate");
    }

    @Test
    void getById_returnsMealEntryWithRecipesAndAdjustments() {
        var potato = ingredientService.create(new IngredientRequestDto(
                "Potato",
                new BigDecimal("77.00"),
                new BigDecimal("2.00"),
                new BigDecimal("17.00"),
                new BigDecimal("0.10"),
                null,
                List.of()));
        var recipe = recipeService.create(new RecipeRequestDto(
                "Mashed potatoes",
                null,
                4,
                List.of(new RecipeComponentRequestDto(
                        RecipeComponentType.INGREDIENT,
                        potato.id(),
                        null,
                        new BigDecimal("500.00"),
                        "g")),
                List.of()));
        var dish = dishService.create(new DishRequestDto("Mashed potatoes plate", null));
        var mealEntry = saveMealEntry(dish.id(), LocalDateTime.of(2026, 5, 18, 13, 30), "At home");

        RecipeComponentEntity component = recipeComponentRepository.findByRecipeId(recipe.id()).getFirst();
        RecipeEntity recipeEntity = recipeRepository.findById(recipe.id()).orElseThrow();
        DishEntity dishEntity = dishRepository.findById(dish.id()).orElseThrow();
        MealEntryEntity mealEntryEntity = mealEntryRepository.findById(mealEntry.getId()).orElseThrow();

        MealEntryRecipeEntity mealEntryRecipe = new MealEntryRecipeEntity();
        mealEntryRecipe.setMealEntry(mealEntryEntity);
        mealEntryRecipe.setRecipe(recipeEntity);
        mealEntryRecipe.setServingAmount(new BigDecimal("2.00"));
        LocalDateTime now = LocalDateTime.now();
        mealEntryRecipe.setCreatedAt(now);
        mealEntryRecipe.setUpdatedAt(now);
        mealEntryRecipe = mealEntryRecipeRepository.save(mealEntryRecipe);

        MealEntryRecipeAdjustmentEntity removeMilk = new MealEntryRecipeAdjustmentEntity();
        removeMilk.setMealEntryRecipe(mealEntryRecipe);
        removeMilk.setAdjustmentType(MealEntryRecipeAdjustmentType.REMOVE);
        removeMilk.setRecipeComponent(component);
        removeMilk.setCreatedAt(now);
        removeMilk.setUpdatedAt(now);
        mealEntryRecipeAdjustmentRepository.save(removeMilk);

        var loaded = mealEntryService.getById(mealEntry.getId());

        assertThat(loaded.dishId()).isEqualTo(dish.id());
        assertThat(loaded.dishName()).isEqualTo("Mashed potatoes plate");
        assertThat(loaded.notes()).isEqualTo("At home");
        assertThat(loaded.recipes()).hasSize(1);
        assertThat(loaded.recipes().getFirst().recipeId()).isEqualTo(recipe.id());
        assertThat(loaded.recipes().getFirst().recipeName()).isEqualTo("Mashed potatoes");
        assertThat(loaded.recipes().getFirst().servingAmount()).isEqualByComparingTo("2.00");
        assertThat(loaded.recipes().getFirst().adjustments()).hasSize(1);
        assertThat(loaded.recipes().getFirst().adjustments().getFirst().adjustmentType())
                .isEqualTo(MealEntryRecipeAdjustmentType.REMOVE);
        assertThat(loaded.recipes().getFirst().adjustments().getFirst().recipeComponentId())
                .isEqualTo(component.getId());
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        assertThatThrownBy(() -> mealEntryService.getById(999_999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Meal entry not found");
    }

    private MealEntryEntity saveMealEntry(Integer dishId, LocalDateTime eatenAt, String notes) {
        DishEntity dish = dishRepository.findById(dishId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        MealEntryEntity entity = new MealEntryEntity();
        entity.setDish(dish);
        entity.setEatenAt(eatenAt);
        entity.setNotes(notes);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return mealEntryRepository.save(entity);
    }
}
