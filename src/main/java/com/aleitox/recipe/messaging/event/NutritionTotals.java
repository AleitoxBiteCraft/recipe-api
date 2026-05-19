package com.aleitox.recipe.messaging.event;

import java.math.BigDecimal;

public record NutritionTotals(
        BigDecimal calories,
        BigDecimal proteins,
        BigDecimal carbohydrates,
        BigDecimal fats
) {
    public static NutritionTotals zero() {
        return new NutritionTotals(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    public NutritionTotals add(NutritionTotals other) {
        return new NutritionTotals(
                calories.add(other.calories()),
                proteins.add(other.proteins()),
                carbohydrates.add(other.carbohydrates()),
                fats.add(other.fats()));
    }
}
