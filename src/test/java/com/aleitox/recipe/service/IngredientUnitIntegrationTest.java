package com.aleitox.recipe.service;

import com.aleitox.recipe.domain.RecipeComponentType;
import com.aleitox.recipe.dto.IngredientRequestDto;
import com.aleitox.recipe.dto.IngredientUnitRequestDto;
import com.aleitox.recipe.dto.RecipeComponentRequestDto;
import com.aleitox.recipe.dto.RecipeRequestDto;
import com.aleitox.recipe.dto.RecipeStepRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class IngredientUnitIntegrationTest {

    @Autowired
    private IngredientService ingredientService;

    @Autowired
    private RecipeService recipeService;

    @Test
    void getAllRecipes_whenComponentUsesIngredientUnit_returnsNutritionTotals() {
        var wrapper = ingredientService.create(ingredient(
                "Test wonton wrapper",
                List.of(new IngredientUnitRequestDto("unit", new BigDecimal("8.00")))));

        recipeService.create(new RecipeRequestDto(
                "Test dumplings",
                "Countable wrapper test",
                4,
                List.of(new RecipeComponentRequestDto(
                        RecipeComponentType.INGREDIENT,
                        wrapper.id(),
                        null,
                        new BigDecimal("40.00"),
                        "unit")),
                List.of(new RecipeStepRequestDto(1, "Steam until cooked."))));

        var recipes = recipeService.getAll();
        var created = recipes.stream()
                .filter(recipe -> "Test dumplings".equals(recipe.name()))
                .findFirst()
                .orElseThrow();

        assertThat(created.totalCalories()).isEqualByComparingTo("937.60");
        assertThat(created.totalProteins()).isEqualByComparingTo("29.28");
    }

    @Test
    void getIngredientById_includesUnits() {
        var saved = ingredientService.create(ingredient(
                "Wrapper with units",
                List.of(new IngredientUnitRequestDto("unit", new BigDecimal("8.00")))));

        var loaded = ingredientService.getById(saved.id());

        assertThat(loaded.units()).hasSize(1);
        assertThat(loaded.units().getFirst().unit()).isEqualTo("unit");
        assertThat(loaded.units().getFirst().gramsPerUnit()).isEqualByComparingTo("8.00");
    }

    @Test
    void createRecipe_whenCountableUnitHasNoConversion_rejectsRequest() {
        var flour = ingredientService.create(ingredient("Plain flour", List.of()));

        assertThatThrownBy(() -> recipeService.create(new RecipeRequestDto(
                "Invalid unit recipe",
                null,
                null,
                List.of(new RecipeComponentRequestDto(
                        RecipeComponentType.INGREDIENT,
                        flour.id(),
                        null,
                        new BigDecimal("2.00"),
                        "unit")),
                List.of(new RecipeStepRequestDto(1, "Mix.")))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No grams-per-unit conversion");
    }

    private static IngredientRequestDto ingredient(String name, List<IngredientUnitRequestDto> units) {
        return new IngredientRequestDto(
                name,
                new BigDecimal("293.00"),
                new BigDecimal("9.15"),
                new BigDecimal("57.10"),
                new BigDecimal("3.50"),
                "test",
                units);
    }
}
