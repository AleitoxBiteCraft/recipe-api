-- =============================
-- 2. DATA POPULATION
-- =============================

-- 2.1 Populate 'ingredient' with Vietnamese salad ingredients and dressing ingredients.
INSERT INTO ingredient (
    id, name, calories_per_100g, proteins_per_100g, carbohydrates_per_100g, fats_per_100g, nutrition_source
) VALUES
    (1, 'Pechuga de pollo', 165.00, 31.00, 0.00, 3.60, 'USDA FoodData Central'),
    (2, 'Repollo morado', 31.00, 1.40, 7.40, 0.20, 'USDA FoodData Central'),
    (3, 'Cebolla morada', 40.00, 1.10, 9.30, 0.10, 'USDA FoodData Central'),
    (4, 'Morron rojo', 31.00, 1.00, 6.00, 0.30, 'USDA FoodData Central'),
    (5, 'Pepino', 15.00, 0.70, 3.60, 0.10, 'USDA FoodData Central'),
    (6, 'Zanahoria', 41.00, 0.90, 9.60, 0.20, 'USDA FoodData Central'),
    (7, 'Jalapeno', 29.00, 0.90, 6.50, 0.40, 'USDA FoodData Central'),
    (8, 'Menta', 44.00, 3.30, 8.40, 0.70, 'USDA FoodData Central'),
    (9, 'Cilantro', 23.00, 2.10, 3.70, 0.50, 'USDA FoodData Central'),
    (10, 'Mani', 567.00, 25.80, 16.10, 49.20, 'USDA FoodData Central'),
    (11, 'Lima', 30.00, 0.70, 10.50, 0.20, 'USDA FoodData Central'),
    (12, 'Vinagre de arroz', 18.00, 0.00, 0.00, 0.00, 'USDA FoodData Central'),
    (13, 'Fish sauce', 35.00, 5.60, 3.60, 0.00, 'USDA FoodData Central'),
    (14, 'Aceite de girasol', 884.00, 0.00, 0.00, 100.00, 'USDA FoodData Central'),
    (15, 'Azucar', 387.00, 0.00, 100.00, 0.00, 'USDA FoodData Central'),
    (16, 'Ajo', 149.00, 6.40, 33.10, 0.50, 'USDA FoodData Central'),
    (17, 'Chile rojo', 40.00, 1.90, 8.80, 0.40, 'USDA FoodData Central');

-- 2.2 Create dish and recipes.
INSERT INTO dish (
    id, name, description
) VALUES (
    1,
    'Ensalada vietnamita con aderezo hit',
    'Ensalada fresca para 3 personas con vegetales, pollo desmechado y aderezo estilo vietnamita.'
);

INSERT INTO recipe (
    id, name, description
) VALUES
    (1, 'Aderezo hit', 'Aderezo con lima, fish sauce y aceite para ensalada vietnamita.'),
    (2, 'Ensalada vietnamita con aderezo hit', 'Receta principal para 3 personas con pollo y vegetales.'),
    (3, 'Pollo hervido desmenuzado', 'Pechuga hervida y desmenuzada para usar en ensaladas y otras preparaciones.');

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES (
    1, 1, 1
);

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES (
    2, 1, 2
);

-- 2.3 Link ingredients and nested recipe in 'recipe_component'.
INSERT INTO recipe_component (
    id, recipe_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (1, 1, 'INGREDIENT', 11, null, 30.00, 'ml'),
    (2, 1, 'INGREDIENT', 12, null, 30.00, 'ml'),
    (3, 1, 'INGREDIENT', 13, null, 60.00, 'ml'),
    (4, 1, 'INGREDIENT', 14, null, 60.00, 'ml'),
    (5, 1, 'INGREDIENT', 15, null, 15.00, 'g'),
    (6, 1, 'INGREDIENT', 16, null, 5.00, 'g'),
    (7, 1, 'INGREDIENT', 17, null, 10.00, 'g'),
    (8, 3, 'INGREDIENT', 1, null, 250.00, 'g'),
    (9, 2, 'INGREDIENT', 2, null, 500.00, 'g'),
    (10, 2, 'INGREDIENT', 3, null, 100.00, 'g'),
    (11, 2, 'INGREDIENT', 4, null, 120.00, 'g'),
    (12, 2, 'INGREDIENT', 5, null, 250.00, 'g'),
    (13, 2, 'INGREDIENT', 6, null, 120.00, 'g'),
    (14, 2, 'INGREDIENT', 7, null, 25.00, 'g'),
    (15, 2, 'INGREDIENT', 8, null, 25.00, 'g'),
    (16, 2, 'INGREDIENT', 9, null, 25.00, 'g'),
    (17, 2, 'INGREDIENT', 10, null, 50.00, 'g'),
    (18, 2, 'RECIPE', null, 1, 1.00, 'batch'),
    (19, 2, 'RECIPE', null, 3, 1.00, 'batch');

-- 2.4 Add step-by-step instructions in 'recipe_step'.
INSERT INTO recipe_step (
    id, recipe_id, step_order, description
) VALUES
    (1, 1, 1, 'Mezclar todos los ingredientes del aderezo en un bowl.'),
    (2, 1, 2, 'Dejar reposar 10 minutos para que los sabores se concentren.'),
    (3, 3, 1, 'Poner en una olla 2 litros de agua y el pollo.'),
    (4, 3, 2, 'Una vez que hierva, colocar una tapa y apagar el fuego.'),
    (5, 3, 3, 'Dejar reposando de 20 a 50 minutos totales para que se cocine y quede jugoso.'),
    (6, 3, 4, 'Sacar el pollo y desmecharlo.'),
    (7, 2, 1, 'Cortar repollo fino, cebolla en brunoise, morron en juliana, pepino en laminas y zanahoria en juliana.'),
    (8, 2, 2, 'Picar jalapeno sin semillas, menta y cilantro. Tostar y picar el mani.'),
    (9, 2, 3, 'Mezclar todos los ingredientes de la ensalada (excepto mani) con la mitad del aderezo y reposar 5 minutos.'),
    (10, 2, 4, 'Antes de servir, agregar el resto del aderezo, mezclar y terminar con mani.');
