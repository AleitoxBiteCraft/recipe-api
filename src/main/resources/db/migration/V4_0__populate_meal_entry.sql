-- ============================================
-- Meal log seed (depende de V3_0)
-- Comida: plato "Dumpling de cerdo y langostinos" (dish 6, recipe 17).
-- Receta seguida tal cual, con dos ajustes en la linea:
--   REMOVE recipe_component 104 (Langostinos, 220 g)
--   REMOVE recipe_component 105 (Carne picada de cerdo, 460 g catalogo)
--   ADD ingrediente 34 (Carne picada de cerdo) 680 g
-- ============================================

INSERT INTO meal_entry (
    id, dish_id, eaten_at, notes
) VALUES (
    1,
    6,
    '2026-05-18 20:30:00',
    'Dumpling de cerdo y langostinos: receta tal cual, sin langostinos; carne picada 680 g en lugar de 460 g.'
);

INSERT INTO meal_entry_recipe (
    id, meal_entry_id, recipe_id, serving_amount
) VALUES (
    1,
    1,
    17,
    4.00
);

INSERT INTO meal_entry_recipe_adjustment (
    id, meal_entry_recipe_id, adjustment_type, recipe_component_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (1, 1, 'REMOVE', 104, null, null, null, null, null),
    (2, 1, 'REMOVE', 105, null, null, null, null, null),
    (3, 1, 'ADD', null, 'INGREDIENT', 34, null, 680.00, 'g');
