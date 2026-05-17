-- =============================
-- FOOD RECIPE APP SCHEMA
-- MySQL
-- =============================

CREATE TABLE ingredient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    calories_per_100g DECIMAL(10,2) NOT NULL,
    proteins_per_100g DECIMAL(10,2) NOT NULL,
    carbohydrates_per_100g DECIMAL(10,2) NOT NULL,
    fats_per_100g DECIMAL(10,2) NOT NULL,
    nutrition_source VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_ingredient_name UNIQUE (name),
    CONSTRAINT chk_ingredient_calories CHECK (calories_per_100g >= 0),
    CONSTRAINT chk_ingredient_proteins CHECK (proteins_per_100g >= 0),
    CONSTRAINT chk_ingredient_carbohydrates CHECK (carbohydrates_per_100g >= 0),
    CONSTRAINT chk_ingredient_fats CHECK (fats_per_100g >= 0)
);

CREATE TABLE ingredient_unit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT NOT NULL,
    unit VARCHAR(50) NOT NULL,
    grams_per_unit DECIMAL(10,4) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingredient_unit_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE,

    CONSTRAINT uq_ingredient_unit
        UNIQUE (ingredient_id, unit),

    CONSTRAINT chk_ingredient_unit_grams
        CHECK (grams_per_unit > 0)
);

CREATE TABLE recipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    serving INT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_recipe_name UNIQUE (name),
    CONSTRAINT chk_recipe_serving CHECK (serving IS NULL OR serving > 0)
);

CREATE TABLE tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_tag_name UNIQUE (name)
);

CREATE TABLE recipe_tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    tag_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_tag_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT fk_recipe_tag_tag
        FOREIGN KEY (tag_id) REFERENCES tag(id),

    CONSTRAINT uq_recipe_tag
        UNIQUE (recipe_id, tag_id)
);

CREATE TABLE recipe_component (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    ingredient_id INT NULL,
    child_recipe_id INT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'g',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_component_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT fk_recipe_component_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),

    CONSTRAINT fk_recipe_component_child_recipe
        FOREIGN KEY (child_recipe_id) REFERENCES recipe(id),

    CONSTRAINT uq_recipe_component_ingredient
        UNIQUE (recipe_id, component_type, ingredient_id),

    CONSTRAINT uq_recipe_component_child_recipe
        UNIQUE (recipe_id, component_type, child_recipe_id),

    CONSTRAINT chk_recipe_component_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_recipe_component_type
        CHECK (component_type IN ('INGREDIENT', 'RECIPE')),

    CONSTRAINT chk_recipe_component_reference
        CHECK (
            (component_type = 'INGREDIENT' AND ingredient_id IS NOT NULL AND child_recipe_id IS NULL)
            OR
            (component_type = 'RECIPE' AND ingredient_id IS NULL AND child_recipe_id IS NOT NULL)
        ),

    CONSTRAINT chk_recipe_component_no_self_reference
        CHECK (child_recipe_id IS NULL OR recipe_id <> child_recipe_id)
);

CREATE TABLE recipe_step (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipe_id INT NOT NULL,
    step_order INT NOT NULL,
    description TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipe_step_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT uq_recipe_step_order
        UNIQUE (recipe_id, step_order),

    CONSTRAINT chk_recipe_step_order
        CHECK (step_order > 0)
);

CREATE TABLE dish (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_dish_name UNIQUE (name)
);

CREATE TABLE dish_recipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dish_id INT NOT NULL,
    recipe_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_dish_recipe_dish
        FOREIGN KEY (dish_id) REFERENCES dish(id),

    CONSTRAINT fk_dish_recipe_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT uq_dish_recipe
        UNIQUE (dish_id, recipe_id)
);

CREATE TABLE meal_entry (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dish_id INT NULL,
    eaten_at DATETIME NOT NULL,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_meal_entry_dish
        FOREIGN KEY (dish_id) REFERENCES dish(id)
);

CREATE TABLE meal_entry_recipe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    meal_entry_id INT NOT NULL,
    recipe_id INT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'g',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_meal_entry_recipe_meal_entry
        FOREIGN KEY (meal_entry_id) REFERENCES meal_entry(id),

    CONSTRAINT fk_meal_entry_recipe_recipe
        FOREIGN KEY (recipe_id) REFERENCES recipe(id),

    CONSTRAINT chk_meal_entry_recipe_quantity
        CHECK (quantity > 0)
);

-- =============================
-- INDEXES
-- =============================

CREATE INDEX idx_ingredient_unit_ingredient_id
    ON ingredient_unit(ingredient_id);

CREATE INDEX idx_recipe_component_recipe_id
    ON recipe_component(recipe_id);

CREATE INDEX idx_recipe_component_ingredient_id
    ON recipe_component(ingredient_id);

CREATE INDEX idx_recipe_component_child_recipe_id
    ON recipe_component(child_recipe_id);

CREATE INDEX idx_recipe_step_recipe_id
    ON recipe_step(recipe_id);

CREATE INDEX idx_recipe_tag_recipe_id
    ON recipe_tag(recipe_id);

CREATE INDEX idx_recipe_tag_tag_id
    ON recipe_tag(tag_id);

CREATE INDEX idx_dish_recipe_dish_id
    ON dish_recipe(dish_id);

CREATE INDEX idx_dish_recipe_recipe_id
    ON dish_recipe(recipe_id);

CREATE INDEX idx_meal_entry_dish_id
    ON meal_entry(dish_id);

CREATE INDEX idx_meal_entry_eaten_at
    ON meal_entry(eaten_at);

CREATE INDEX idx_meal_entry_recipe_meal_entry_id
    ON meal_entry_recipe(meal_entry_id);

CREATE INDEX idx_meal_entry_recipe_recipe_id
    ON meal_entry_recipe(recipe_id);