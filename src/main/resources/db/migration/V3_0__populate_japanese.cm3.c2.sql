-- ============================================
-- Population: clase japonesa (depende de V2_0)
-- Ejecutar despues de V2_0__populate_tables.sql
-- Reutiliza ingredientes 1-37, recetas 1-9 y platos 1-4 insertados por V2_0.
-- ============================================
-- Japanese (V3): ingredientes (38-62). Agua, sal y pimienta solo en pasos.
-- Reutilizados entre otros: Ajo (16), Zanahoria (6), Jengibre (21), Vinagre de arroz (12), Azucar (15),
-- Salsa de soja (19), Carne picada de cerdo (34), Aceite de girasol (14), Cebolla de verdeo (22),
-- Brotes de soja (36), Langostinos (33), Aceite de sesamo (28), Lima (11), Limon (24).
INSERT INTO ingredient (
    id, name, calories_per_100g, proteins_per_100g, carbohydrates_per_100g, fats_per_100g, nutrition_source
) VALUES
    (38, 'Huesos de pollo', 132.00, 16.00, 0.00, 7.40, 'USDA FoodData Central'),
    (39, 'Shiitake seco', 296.00, 9.60, 75.40, 1.70, 'USDA FoodData Central'),
    (40, 'Cebolla', 40.00, 1.10, 9.30, 0.10, 'USDA FoodData Central'),
    (41, 'Perejil', 36.00, 3.00, 6.30, 0.80, 'USDA FoodData Central'),
    (42, 'Miso blanco (shiro)', 198.00, 11.70, 26.50, 6.10, 'USDA FoodData Central'),
    (43, 'Salsa inglesa', 77.00, 0.00, 19.00, 0.00, 'USDA FoodData Central'),
    (44, 'Grasa de cerdo', 902.00, 0.00, 0.00, 100.00, 'USDA FoodData Central'),
    (45, 'Panceta de cerdo', 518.00, 9.30, 0.00, 53.00, 'USDA FoodData Central'),
    (46, 'Mirin', 230.00, 0.50, 36.70, 0.00, 'USDA FoodData Central'),
    (47, 'Canela en polvo', 247.00, 4.00, 80.60, 1.20, 'USDA FoodData Central'),
    (48, 'Clavo de olor en polvo', 323.00, 5.98, 61.38, 13.07, 'USDA FoodData Central'),
    (49, 'Anis estrellado en polvo', 337.00, 17.60, 50.00, 15.90, 'USDA FoodData Central'),
    (50, 'Huevo', 143.00, 12.60, 0.70, 9.50, 'USDA FoodData Central'),
    (51, 'Fideos de ramen', 138.00, 4.50, 27.80, 0.60, 'USDA FoodData Central'),
    (52, 'Choclo cocido desgranado', 86.00, 3.30, 18.70, 1.20, 'USDA FoodData Central'),
    (53, 'Choclo en lata en granos', 72.00, 2.30, 15.10, 0.80, 'USDA FoodData Central'),
    (54, 'Semillas de sesamo', 656.00, 19.40, 24.00, 58.00, 'USDA FoodData Central'),
    (55, 'Salsa sriracha', 100.00, 1.90, 19.00, 0.90, 'USDA FoodData Central'),
    (56, 'Mayonesa', 680.00, 1.00, 0.60, 75.00, 'USDA FoodData Central'),
    (57, 'Atun en lata escurrido', 116.00, 25.50, 0.10, 0.80, 'USDA FoodData Central'),
    (58, 'Salsa de ostras', 51.00, 1.40, 10.70, 0.30, 'USDA FoodData Central'),
    (59, 'Aceite de mani', 884.00, 0.00, 0.00, 100.00, 'USDA FoodData Central'),
    (60, 'Tapas wonton', 293.00, 9.15, 57.10, 3.50, 'USDA FoodData Central'),
    (61, 'Arroz Koshihikari', 356.00, 6.50, 79.00, 0.50, 'USDA FoodData Central'),
    (62, 'Salmon o trucha fresco sin piel', 208.00, 20.40, 0.00, 13.40, 'USDA FoodData Central');

INSERT INTO tag (
    id, name
) VALUES (
    2, 'Japanese'
);

INSERT INTO recipe (
    id, name, description
) VALUES
    (10, 'Caldo pollo base (ramen)', 'Primera coccion de huesos con aromaticos; el liquido colado sirve como base del caldo de ramen.'),
    (11, 'Caldo ramen', 'Liquido infusionado con shiitake y cebolla, luego miso y salsas. Usa una tanda completa de caldo pollo base.'),
    (12, 'Sofrito opcional de cerdo (ramen)', 'Carne picada dorada con ajo, jengibre y vinagre de arroz para sumar al bowl; paso opcional del ramen.'),
    (13, 'Chashu de panceta para ramen', 'Panceta cocida en salsa dulce salada tipo chashu para servir sobre ramen.'),
    (14, 'Huevos soft-boil para ramen', 'Huevos con clara firme y yema cremosa para ramen.'),
    (15, 'Huevos marinados para ramen (ajitsuke tamago)', 'Huevos pelados marinados en mezcla de soja y mirin.'),
    (16, 'Ramen de cerdo', 'Bowl para 2 personas con caldo, fideos, chashu, huevo marinado y toppings.'),
    (17, 'Dumpling de cerdo y langostinos', 'Relleno de cerdo y langostinos en tapas wonton; aproximadamente 40 unidades.'),
    (18, 'Crispy rice', 'Arroz para sushi compactado, refrigerado y frito en cuadrados para servir con tartar o poke.'),
    (19, 'Tartar de trucha o salmon', 'Mezcla de pescado en cubos con sriracha, mayonesa, aceite de sesamo, verdeo y semillas.'),
    (20, 'Poke de salmon', 'Variante de pescado en cubos con soja, mirin, vinagre de arroz, sriracha, jengibre y aceite de sesamo.'),
    (21, 'Topping de atun en lata', 'Atun desmenuzado con sriracha, mayonesa, jugo de limon y aromatizantes.'),
    (22, 'Tartar de trucha (variante con lima)', 'Variante con jengibre, salsa de soja, aceite de mani y semillas; ralladura de lima en los pasos. Inspirada en notas de clase.');

INSERT INTO recipe_component (
    id, recipe_id, component_type, ingredient_id, child_recipe_id, quantity, unit
) VALUES
    (64, 10, 'INGREDIENT', 38, null, 200.00, 'g'),
    (65, 10, 'INGREDIENT', 41, null, 30.00, 'g'),
    (66, 10, 'INGREDIENT', 16, null, 8.00, 'g'),
    (67, 10, 'INGREDIENT', 40, null, 150.00, 'g'),
    (68, 10, 'INGREDIENT', 6, null, 100.00, 'g'),
    (69, 11, 'RECIPE', null, 10, 1.00, 'batch'),
    (70, 11, 'INGREDIENT', 39, null, 25.00, 'g'),
    (71, 11, 'INGREDIENT', 40, null, 80.00, 'g'),
    (72, 11, 'INGREDIENT', 42, null, 30.00, 'g'),
    (73, 11, 'INGREDIENT', 15, null, 5.00, 'g'),
    (74, 11, 'INGREDIENT', 19, null, 30.00, 'ml'),
    (75, 11, 'INGREDIENT', 43, null, 10.00, 'ml'),
    (76, 12, 'INGREDIENT', 44, null, 20.00, 'g'),
    (77, 12, 'INGREDIENT', 34, null, 50.00, 'g'),
    (78, 12, 'INGREDIENT', 16, null, 6.00, 'g'),
    (79, 12, 'INGREDIENT', 21, null, 12.00, 'g'),
    (80, 12, 'INGREDIENT', 12, null, 22.00, 'ml'),
    (81, 13, 'INGREDIENT', 45, null, 200.00, 'g'),
    (82, 13, 'INGREDIENT', 16, null, 6.00, 'g'),
    (83, 13, 'INGREDIENT', 21, null, 8.00, 'g'),
    (84, 13, 'INGREDIENT', 19, null, 75.00, 'ml'),
    (85, 13, 'INGREDIENT', 46, null, 50.00, 'ml'),
    (86, 13, 'INGREDIENT', 15, null, 22.00, 'g'),
    (87, 13, 'INGREDIENT', 47, null, 1.00, 'g'),
    (88, 13, 'INGREDIENT', 48, null, 1.00, 'g'),
    (89, 13, 'INGREDIENT', 49, null, 1.00, 'g'),
    (90, 14, 'INGREDIENT', 50, null, 110.00, 'g'),
    (91, 15, 'RECIPE', null, 14, 1.00, 'batch'),
    (92, 15, 'INGREDIENT', 19, null, 85.00, 'ml'),
    (93, 15, 'INGREDIENT', 46, null, 60.00, 'ml'),
    (94, 16, 'RECIPE', null, 11, 1.00, 'batch'),
    (95, 16, 'RECIPE', null, 13, 1.00, 'batch'),
    (96, 16, 'RECIPE', null, 15, 1.00, 'batch'),
    (97, 16, 'INGREDIENT', 51, null, 200.00, 'g'),
    (98, 16, 'INGREDIENT', 52, null, 100.00, 'g'),
    (99, 16, 'INGREDIENT', 36, null, 80.00, 'g'),
    (100, 16, 'INGREDIENT', 22, null, 15.00, 'g'),
    (101, 16, 'INGREDIENT', 21, null, 6.00, 'g'),
    (102, 16, 'INGREDIENT', 54, null, 10.00, 'g'),
    (103, 17, 'INGREDIENT', 39, null, 35.00, 'g'),
    (104, 17, 'INGREDIENT', 33, null, 220.00, 'g'),
    (105, 17, 'INGREDIENT', 34, null, 460.00, 'g'),
    (106, 17, 'INGREDIENT', 15, null, 17.00, 'g'),
    (107, 17, 'INGREDIENT', 19, null, 30.00, 'ml'),
    (108, 17, 'INGREDIENT', 58, null, 56.00, 'ml'),
    (109, 17, 'INGREDIENT', 21, null, 45.00, 'g'),
    (110, 17, 'INGREDIENT', 22, null, 30.00, 'g'),
    (111, 17, 'INGREDIENT', 59, null, 22.00, 'ml'),
    (112, 17, 'INGREDIENT', 43, null, 8.00, 'ml'),
    (113, 17, 'INGREDIENT', 60, null, 40.00, 'unit'),
    (114, 18, 'INGREDIENT', 61, null, 300.00, 'g'),
    (115, 18, 'INGREDIENT', 12, null, 30.00, 'ml'),
    (116, 18, 'INGREDIENT', 15, null, 8.00, 'g'),
    (117, 18, 'INGREDIENT', 14, null, 45.00, 'ml'),
    (118, 19, 'INGREDIENT', 62, null, 250.00, 'g'),
    (119, 19, 'INGREDIENT', 55, null, 15.00, 'ml'),
    (120, 19, 'INGREDIENT', 56, null, 30.00, 'ml'),
    (121, 19, 'INGREDIENT', 28, null, 10.00, 'ml'),
    (122, 19, 'INGREDIENT', 22, null, 25.00, 'g'),
    (123, 19, 'INGREDIENT', 54, null, 7.00, 'g'),
    (124, 20, 'INGREDIENT', 62, null, 250.00, 'g'),
    (125, 20, 'INGREDIENT', 19, null, 22.00, 'ml'),
    (126, 20, 'INGREDIENT', 28, null, 21.00, 'ml'),
    (127, 20, 'INGREDIENT', 46, null, 23.00, 'ml'),
    (128, 20, 'INGREDIENT', 12, null, 22.00, 'ml'),
    (129, 20, 'INGREDIENT', 55, null, 15.00, 'ml'),
    (130, 20, 'INGREDIENT', 21, null, 12.00, 'g'),
    (131, 21, 'INGREDIENT', 57, null, 350.00, 'g'),
    (132, 21, 'INGREDIENT', 55, null, 45.00, 'ml'),
    (133, 21, 'INGREDIENT', 56, null, 60.00, 'ml'),
    (134, 21, 'INGREDIENT', 24, null, 15.00, 'ml'),
    (135, 21, 'INGREDIENT', 28, null, 5.00, 'ml'),
    (136, 21, 'INGREDIENT', 22, null, 40.00, 'g'),
    (137, 21, 'INGREDIENT', 54, null, 18.00, 'g'),
    (138, 22, 'INGREDIENT', 62, null, 150.00, 'g'),
    (139, 22, 'INGREDIENT', 21, null, 15.00, 'g'),
    (140, 22, 'INGREDIENT', 19, null, 45.00, 'ml'),
    (141, 22, 'INGREDIENT', 59, null, 15.00, 'ml'),
    (142, 22, 'INGREDIENT', 54, null, 10.00, 'g');

INSERT INTO recipe_tag (
    id, recipe_id, tag_id
) VALUES
    (7, 10, 2),
    (8, 11, 2),
    (9, 12, 2),
    (10, 13, 2),
    (11, 14, 2),
    (12, 15, 2),
    (13, 16, 2),
    (14, 17, 2),
    (15, 18, 2),
    (16, 19, 2),
    (17, 20, 2),
    (18, 21, 2),
    (19, 22, 2);

INSERT INTO dish (
    id, name, description
) VALUES (
    5,
    'Ramen de cerdo',
    'Ramen casero para 2 con caldo, chashu, huevos marinados, fideos y toppings; opcionalmente sumar el sofrito de cerdo (receta aparte).'
),
(
    6,
    'Dumpling de cerdo y langostinos',
    'Dumplings al vapor rellenos de cerdo, langostinos y shiitake; sirven con sriracha u otra salsa picante.'
),
(
    7,
    'Tartar de salmon con crispy rice',
    'Base de arroz frito y tartar de trucha o salmon; el plato enlaza la receta de crispy rice y la de tartar clasica (se puede sustituir por poke o variante con lima).'
);

INSERT INTO dish_recipe (
    id, dish_id, recipe_id
) VALUES
    (8, 5, 16),
    (9, 6, 17),
    (10, 7, 18),
    (11, 7, 19);

INSERT INTO recipe_step (
    id, recipe_id, step_order, description
) VALUES
    (30, 10, 1, 'En una olla grande cubrir huesos, perejil, ajo, cebolla en cuartos y zanahoria con agua (aprox. 1200 ml). Llevar a hervor.'),
    (31, 10, 2, 'Cocinar a fuego medio 25 a 30 minutos; espumar si busca caldo muy claro. Colar descartando solidos y reservar solo el liquido.'),
    (32, 10, 3, 'Guardar ese fondo hasta continuar la receta Caldo ramen (o congelar en porciones).'),
    (33, 11, 1, 'Con el liquido del caldo pollo base, incorporar shiitake seco (rehidratar si hace falta) y la cebolla en juliana. Cocinar fuego suave unos 25 minutos.'),
    (34, 11, 2, 'Colar el liquido; reservar los shiitake para otros usos si gusta.'),
    (35, 11, 3, 'Devolver el liquido a la olla, agregar miso, azucar, salsa inglesa y salsa de soja y hervir suavemente unos 3 minutos. Rectificar de sal.'),
    (36, 11, 4, 'Atajo: partiendo de caldo casero o comprado (~1 L) se puede hacer la infusion con shiitake y cebolla y el mismo acabado con miso y salsas.'),
    (37, 12, 1, 'Fundir la grasa de cerdo y dorar la carne picada a fuego medio unos 4 minutos.'),
    (38, 12, 2, 'Sumar ajo picado y jengibre rallado y cocinar 1 minuto mas.'),
    (39, 12, 3, 'Terminar agregando el vinagre de arroz unos instantes antes de usar como base sabrosa opcional para el ramen.'),
    (40, 13, 1, 'Semi congelar la panceta y cortar en lonjas finas (ayuda a un corte limpio).'),
    (41, 13, 2, 'Hervir agua en olla u hervor rapido con las lonjas tapado segun tecnica habitual; escurrir.'),
    (42, 13, 3, 'Sellar la panceta en sarten caliente por ambos lados.'),
    (43, 13, 4, 'Agregar salsa de soja, mirin, azucar y las especias en polvo; volver a colocar la panceta.'),
    (44, 13, 5, 'Cocinar a fuego bajo tapado o destapado segun se busque glaseado, hasta que el liquido se reduzca y la carne tome color; voltear cada tanto.'),
    (45, 13, 6, 'Enfriar y cortar contra la fibra para montar sobre el ramen.'),
    (46, 14, 1, 'Llevar agua con un chorrito de vinagre a hervor fuerte.'),
    (47, 14, 2, 'Introducir los huevos cuidadosamente y cronometrar entre 6 y 6 minutos y medio para blanco firme y yema fluida.'),
    (48, 14, 3, 'Pasarlos a agua con hielo para cortar la coccion y pelar con cuidado.'),
    (49, 15, 1, 'Mezclar salsa de soja y mirin en un recipiente estrecho para cubrir los huevos pelados.'),
    (50, 15, 2, 'Sumergir los huevos y marinar en heladera entre 6 y 72 horas segun intensidad deseada; rotar cada tanto si conviene.'),
    (51, 16, 1, 'Preparar o recalentar el Caldo ramen, el Chashu, los Huevos marinados y (opcional) el Sofrito de cerdo antes de montar.'),
    (52, 16, 2, 'Hervir fideos segun paquete, colar bien y dividir entre dos bowls.'),
    (53, 16, 3, 'Rellenar con caldo caliente. Opcional: un sope de sofrito de cerdo al fondo.'),
    (54, 16, 4, 'Cubrir con laminas de chashu, huevo marinado cortado medio, brotes de soja, choclo desgranado (o cantidad equivalente en lata escurrida) y cebolla de verdeo.'),
    (55, 16, 5, 'Espolvorear semillas de sesamo y jengibre rallado extra. Rectificar salsa de soja o miso solo si gusta.'),
    (56, 16, 6, 'Servir muy caliente.'),
    (57, 17, 1, 'Batir bien la carne picada con azucar, salsas, jengibre, cebolla de verdeo, aceite de mani y salsa inglesa 2 a 3 minutos (sin langostinos ni shiitake).'),
    (58, 17, 2, 'Incorporar langostinos picados y shiitake picado o rehidratado y mezclar 1 minuto mas.'),
    (59, 17, 3, 'Tapar y marinar en heladera 4 a 8 horas; humedecer con agua segun textura deseada.'),
    (60, 17, 4, 'Armar canastitas con tapas wonton humedeciendo bordes, colocar sobre papel aluminio en vaporera.'),
    (61, 17, 5, 'Cocinar al vapor 15 a 20 minutos hasta que el relleno este cocido.'),
    (62, 17, 6, 'Servir con salsa sriracha u otra picante al gusto. Pimienta negra al gusto en el relleno si se desea.'),
    (63, 18, 1, 'Cocinar el arroz con agua segun paquete (referencia: unos 12 minutos a fuego medio con tapa).'),
    (64, 18, 2, 'Endulzar y saborizar el arroz caliente mezclando vinagre de arroz y azucar.'),
    (65, 18, 3, 'Extender en bandeja con papel manteca a unos 1 cm, presionar con peso y refrigerar al menos 6 horas.'),
    (66, 18, 4, 'Cortar cuadrados de unos 5 x 5 cm.'),
    (67, 18, 5, 'Freir en aceite caliente 3 a 4 minutos por lado hasta dorar.'),
    (68, 18, 6, 'Escurrir y servir en caliente con tartar, poke o topping elegido.'),
    (69, 19, 1, 'Picar salmon o trucha en cubitos chicos con cuchillo humedo y combinar en bowl con el resto de ingredientes del tartar.'),
    (70, 19, 2, 'Servir sobre crispy rice; opcional palta en capa intermedia entre arroz y pescado.'),
    (71, 19, 3, 'Ideal consumir dentro de la hora una vez montado para que el arroz no se humedezca demasiado.'),
    (72, 20, 1, 'Mezclar pescado con salsas, aceites, vinagre, mirin, sriracha y jengibre hasta integrar.'),
    (73, 20, 2, 'Reposar corto tiempo en heladera o servir al momento sobre crispy rice.'),
    (74, 21, 1, 'Escurrir el atun, desmenuzar y mezclar con sriracha, mayonesa, jugo de limon, aceite de sesamo, cebolla de verdeo y semillas.'),
    (75, 21, 2, 'Servir sobre crispy rice o ensaladas al gusto.'),
    (76, 22, 1, 'Cortar pescado en cubos y mezclar con jengibre, salsa de soja, aceite de mani y semillas hasta integrar.'),
    (77, 22, 2, 'Perfumar rallando la cascara de Lima justo antes de servir sobre crispy rice.');

