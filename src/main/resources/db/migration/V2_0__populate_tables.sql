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
    id, name, description, serving
) VALUES
    (1, 'Aderezo hit', 'Aderezo con lima, fish sauce y aceite para ensalada vietnamita.', null),
    (2, 'Ensalada vietnamita con aderezo hit', 'Receta principal para 3 personas con pollo y vegetales.', 3),
    (3, 'Pollo hervido desmenuzado', 'Pechuga hervida y desmenuzada para usar en ensaladas y otras preparaciones.', null);

-- 2.2.1 Add tags and link to recipes.
INSERT INTO tag (
    id, name
) VALUES (
    1, 'Vietnamese'
);

INSERT INTO recipe_tag (
    id, recipe_id, tag_id
) VALUES (
    1, 2, 1
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

-- 2.5 Populate 'ingredient' with Asian Short Ribs ingredients.
-- Reused existing ingredients: Azucar (15), Ajo (16), Vinagre de arroz (12).
INSERT INTO ingredient (
    id, name, calories_per_100g, proteins_per_100g, carbohydrates_per_100g, fats_per_100g, nutrition_source
) VALUES
    (18, 'Costilla vacuna', 291.00, 18.10, 0.00, 24.50, 'USDA FoodData Central'),
    (19, 'Salsa de soja', 53.00, 8.10, 4.90, 0.60, 'USDA FoodData Central'),
    (20, 'Lemongrass', 99.00, 1.80, 25.30, 0.50, 'USDA FoodData Central'),
    (21, 'Jengibre', 80.00, 1.80, 17.80, 0.80, 'USDA FoodData Central'),
    (22, 'Cebolla de verdeo', 32.00, 1.80, 7.30, 0.20, 'USDA FoodData Central'),
    (23, 'Jugo de naranja', 45.00, 0.70, 10.40, 0.20, 'USDA FoodData Central'),
    (24, 'Limon', 29.00, 1.10, 9.30, 0.30, 'USDA FoodData Central'),
    (25, 'Salsa hoisin', 220.00, 3.30, 45.20, 1.70, 'USDA FoodData Central'),
    (26, 'Pasta de mani', 588.00, 25.00, 20.00, 50.00, 'USDA FoodData Central'),
    (27, 'Miel', 304.00, 0.30, 82.40, 0.00, 'USDA FoodData Central'),
    (28, 'Aceite de sesamo', 884.00, 0.00, 0.00, 100.00, 'USDA FoodData Central'),
    (29, 'Salsa de chili', 84.00, 1.30, 17.30, 0.90, 'USDA FoodData Central');

-- 2.6 Create dish and recipe for Asian Short Ribs.
INSERT INTO dish (
    id, name, description
) VALUES (
    2,
    'Asian Short Ribs',
    'Costillas braseadas estilo asiatico para 4 porciones, con salsa de soja, citricos y hoisin.'
);

INSERT INTO recipe (
    id, name, description, serving
) VALUES (
    4,
    'Asian Short Ribs',
    'Costilla vacuna dorada y cocida lentamente en horno con salsa de soja, aromáticos y hoisin.',
    4
);

-- 2.6.1 Link Vietnamese tag to the recipe and recipe to dish.
INSERT INTO recipe_tag (
    id, recipe_id, tag_id
) VALUES (
    2, 4, 1
);

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES (
    3, 2, 4
);

-- 2.7 Link ingredients in 'recipe_component' for Asian Short Ribs.
INSERT INTO recipe_component (
    id, recipe_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (20, 4, 'INGREDIENT', 18, null, 2000.00, 'g'),
    (21, 4, 'INGREDIENT', 19, null, 250.00, 'ml'),
    (22, 4, 'INGREDIENT', 12, null, 60.00, 'ml'),
    (23, 4, 'INGREDIENT', 16, null, 9.00, 'g'),
    (24, 4, 'INGREDIENT', 20, null, 20.00, 'g'),
    (25, 4, 'INGREDIENT', 15, null, 100.00, 'g'),
    (26, 4, 'INGREDIENT', 22, null, 75.00, 'g'),
    (27, 4, 'INGREDIENT', 21, null, 15.00, 'g'),
    (28, 4, 'INGREDIENT', 23, null, 60.00, 'ml'),
    (29, 4, 'INGREDIENT', 24, null, 30.00, 'ml'),
    (30, 4, 'INGREDIENT', 25, null, 60.00, 'ml');

-- 2.8 Add step-by-step instructions in 'recipe_step' for Asian Short Ribs.
INSERT INTO recipe_step (
    id, recipe_id, step_order, description
) VALUES
    (11, 4, 1, 'Precalentar el horno a 150 C. Sacar la carne de la heladera 30 minutos antes, salpimentar y dorar en una sarten bien caliente.'),
    (12, 4, 2, 'Pasar la carne dorada a una fuente apta horno y agregar salsa de soja, vinagre de arroz, ajo, lemongrass, azucar, cebolla de verdeo, jengibre, jugo de naranja, jugo de limon y salsa hoisin.'),
    (13, 4, 3, 'Verificar que el liquido llegue a 3/4 de la altura de la carne. Si falta, completar con agua. Tapar con papel aluminio y hornear 3 horas aproximadamente hasta que se deshilache.'),
    (14, 4, 4, 'Retirar la grasa de la superficie del liquido de coccion y reservar la carne.'),
    (15, 4, 5, 'Reducir el liquido en hornalla hasta espesar y servir sobre la carne.');

-- 2.9 Populate 'ingredient' with Thai Pineapple Rice ingredients.
-- Reused existing ingredients: Salsa de soja (19), Fish sauce (13), Azucar (15),
-- Aceite de girasol (14), Cebolla morada (3), Ajo (16), Jengibre (21),
-- Pechuga de pollo (1), Morron rojo (4), Cebolla de verdeo (22), Limon (24).
INSERT INTO ingredient (
    id, name, calories_per_100g, proteins_per_100g, carbohydrates_per_100g, fats_per_100g, nutrition_source
) VALUES
    (30, 'Arroz basmati', 365.00, 7.10, 80.00, 0.70, 'USDA FoodData Central'),
    (31, 'Anana', 50.00, 0.50, 13.10, 0.10, 'USDA FoodData Central'),
    (32, 'Curry amarillo en polvo', 325.00, 14.30, 55.80, 14.00, 'USDA FoodData Central'),
    (33, 'Langostinos', 99.00, 24.00, 0.20, 0.30, 'USDA FoodData Central');

-- 2.10 Create dish and recipe for Thai Pineapple Rice.
INSERT INTO dish (
    id, name, description
) VALUES (
    3,
    'Thai Pineapple Rice',
    'Arroz thai salteado con anana, vegetales y salsa umami para 4 personas.'
);

INSERT INTO recipe (
    id, name, description, serving
) VALUES (
    5,
    'Thai Pineapple Rice',
    'Arroz basmati cocido y salteado con anana, vegetales, curry amarillo y langostinos.',
    4
);

-- 2.10.1 Link Vietnamese tag to the recipe and recipe to dish.
INSERT INTO recipe_tag (
    id, recipe_id, tag_id
) VALUES (
    3, 5, 1
);

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES (
    4, 3, 5
);

-- 2.11 Link ingredients in 'recipe_component' for Thai Pineapple Rice.
INSERT INTO recipe_component (
    id, recipe_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (31, 5, 'INGREDIENT', 30, null, 200.00, 'g'),
    (32, 5, 'INGREDIENT', 31, null, 200.00, 'g'),
    (33, 5, 'INGREDIENT', 19, null, 45.00, 'ml'),
    (34, 5, 'INGREDIENT', 13, null, 45.00, 'ml'),
    (35, 5, 'INGREDIENT', 15, null, 25.00, 'g'),
    (36, 5, 'INGREDIENT', 32, null, 4.00, 'g'),
    (37, 5, 'INGREDIENT', 14, null, 30.00, 'ml'),
    (38, 5, 'INGREDIENT', 3, null, 120.00, 'g'),
    (39, 5, 'INGREDIENT', 16, null, 12.00, 'g'),
    (40, 5, 'INGREDIENT', 21, null, 8.00, 'g'),
    (41, 5, 'INGREDIENT', 33, null, 250.00, 'g'),
    (42, 5, 'INGREDIENT', 4, null, 120.00, 'g'),
    (43, 5, 'INGREDIENT', 22, null, 100.00, 'g');

-- 2.12 Add step-by-step instructions in 'recipe_step' for Thai Pineapple Rice.
INSERT INTO recipe_step (
    id, recipe_id, step_order, description
) VALUES
    (16, 5, 1, 'En una olla a fuego medio agregar un chorro de aceite y saltear el arroz 2 minutos. Agregar 350 cc de agua hirviendo y sal.'),
    (17, 5, 2, 'Cuando vuelva a hervir, bajar a minimo y cocinar tapado hasta que absorba el agua. Apagar el fuego y reservar.'),
    (18, 5, 3, 'Cortar la anana en cubos chicos y reservar.'),
    (19, 5, 4, 'Mezclar en un bowl salsa de soja, fish sauce, azucar y curry amarillo.'),
    (20, 5, 5, 'Calentar un wok a fuego fuerte, agregar aceite y cocinar los langostinos en tandas 1 minuto por lado hasta que cambien de color. Retirar.'),
    (21, 5, 6, 'En el mismo wok saltear cebolla, jengibre y ajo por 30 segundos. Agregar morron y cocinar 1 minuto.'),
    (22, 5, 7, 'Agregar la mezcla de salsas, incorporar anana y cebolla de verdeo, cocinar 1 minuto.'),
    (23, 5, 8, 'Agregar el arroz cocido, mezclar bien y rectificar condimentos antes de servir.');

-- 2.13 Populate 'ingredient' with Vietnamese meatballs with vermicelli ingredients.
-- Reused existing ingredients: Fish sauce (13), Azucar (15), Cebolla de verdeo (22),
-- Ajo (16), Jengibre (21), Aceite de girasol (14), Vinagre de arroz (12), Lima (11),
-- Jalapeno (7), Zanahoria (6), Pepino (5), Cilantro (9), Menta (8).
INSERT INTO ingredient (
    id, name, calories_per_100g, proteins_per_100g, carbohydrates_per_100g, fats_per_100g, nutrition_source
) VALUES
    (34, 'Carne picada de cerdo', 263.00, 16.90, 0.00, 21.00, 'USDA FoodData Central'),
    (35, 'Fideos de arroz vermicelli', 364.00, 5.90, 80.10, 0.60, 'USDA FoodData Central'),
    (36, 'Brotes de soja', 30.00, 3.00, 6.00, 0.20, 'USDA FoodData Central'),
    (37, 'Lechuga', 15.00, 1.40, 2.90, 0.20, 'USDA FoodData Central');

-- 2.14 Create dish and recipes for Vietnamese meatballs with vermicelli.
INSERT INTO dish (
    id, name, description
) VALUES (
    4,
    'Albondigas vietnamitas con vermicelli noodles',
    'Plato vietnamita para 2 a 3 personas con albondigas de cerdo, fideos de arroz y salsa Nuoc Cham.'
);

INSERT INTO recipe (
    id, name, description, serving
) VALUES
    (7, 'Albondigas vietnamitas', 'Mini albondigas de cerdo sazonadas con fish sauce, ajo y jengibre.', 2),
    (8, 'Vermicelli noodles', 'Acompanamiento de fideos de arroz con vegetales frescos y hierbas.', null),
    (9, 'Salsa Nuoc Cham', 'Salsa vietnamita de fish sauce, lima, vinagre, azucar, ajo y chile.', null);

-- 2.14.1 Link Vietnamese tag to recipes and recipes to dish.
INSERT INTO recipe_tag (
    id, recipe_id, tag_id
) VALUES
    (4, 7, 1),
    (5, 8, 1),
    (6, 9, 1);

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES
    (5, 4, 7),
    (6, 4, 8),
    (7, 4, 9);

-- 2.15 Link ingredients and nested recipes in 'recipe_component'.
INSERT INTO recipe_component (
    id, recipe_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (44, 7, 'INGREDIENT', 34, null, 300.00, 'g'),
    (45, 7, 'INGREDIENT', 13, null, 15.00, 'ml'),
    (46, 7, 'INGREDIENT', 15, null, 8.00, 'g'),
    (47, 7, 'INGREDIENT', 22, null, 25.00, 'g'),
    (48, 7, 'INGREDIENT', 16, null, 3.00, 'g'),
    (49, 7, 'INGREDIENT', 21, null, 6.00, 'g'),
    (50, 7, 'INGREDIENT', 14, null, 10.00, 'ml'),
    (51, 9, 'INGREDIENT', 15, null, 36.00, 'g'),
    (52, 9, 'INGREDIENT', 13, null, 45.00, 'ml'),
    (53, 9, 'INGREDIENT', 12, null, 30.00, 'ml'),
    (54, 9, 'INGREDIENT', 11, null, 30.00, 'ml'),
    (55, 9, 'INGREDIENT', 7, null, 10.00, 'g'),
    (56, 9, 'INGREDIENT', 16, null, 6.00, 'g'),
    (57, 8, 'INGREDIENT', 35, null, 100.00, 'g'),
    (58, 8, 'INGREDIENT', 36, null, 100.00, 'g'),
    (59, 8, 'INGREDIENT', 37, null, 300.00, 'g'),
    (60, 8, 'INGREDIENT', 9, null, 30.00, 'g'),
    (61, 8, 'INGREDIENT', 8, null, 30.00, 'g'),
    (62, 8, 'INGREDIENT', 6, null, 60.00, 'g'),
    (63, 8, 'INGREDIENT', 5, null, 125.00, 'g');

-- 2.16 Add step-by-step instructions in 'recipe_step'.
INSERT INTO recipe_step (
    id, recipe_id, step_order, description
) VALUES
    (24, 7, 1, 'Mezclar carne picada de cerdo, fish sauce, azucar, cebolla de verdeo, ajo, jengibre, sal y pimienta en un bowl.'),
    (25, 7, 2, 'Probar condimento cocinando una mini bolita y ajustar si hace falta. Formar 12 mini patties o albondigas.'),
    (26, 7, 3, 'Calentar sarten a fuego medio-alto con un chorrito de aceite y cocinar 3 minutos de cada lado hasta dorar y cocinar por completo.'),
    (27, 9, 1, 'Mezclar azucar, fish sauce, vinagre de arroz, jugo de lima, agua, jalapeno picado y ajo picado hasta disolver el azucar.'),
    (28, 8, 1, 'Cocinar los fideos vermicelli segun paquete, colar y enfriar.'),
    (29, 8, 2, 'Preparar acompanamiento con brotes de soja, lechuga, cilantro o menta, zanahoria y pepino en juliana.');
