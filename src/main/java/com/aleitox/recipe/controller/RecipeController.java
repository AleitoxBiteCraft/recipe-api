package com.aleitox.recipe.controller;

import com.aleitox.recipe.dto.RecipeComponentRequestDto;
import com.aleitox.recipe.dto.RecipeComponentResponseDto;
import com.aleitox.recipe.dto.RecipeRequestDto;
import com.aleitox.recipe.dto.RecipeResponseDto;
import com.aleitox.recipe.dto.RecipeStepRequestDto;
import com.aleitox.recipe.dto.RecipeStepResponseDto;
import com.aleitox.recipe.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDto> create(@Valid @RequestBody RecipeRequestDto request) {
        RecipeResponseDto created = recipeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RecipeResponseDto>> getAll() {
        return ResponseEntity.ok(recipeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> update(@PathVariable("id") Integer id,
                                                    @Valid @RequestBody RecipeRequestDto request) {
        return ResponseEntity.ok(recipeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{recipeId}/components")
    public ResponseEntity<RecipeComponentResponseDto> addComponent(@PathVariable("recipeId") Integer recipeId,
                                                                   @Valid @RequestBody RecipeComponentRequestDto request) {
        RecipeComponentResponseDto created = recipeService.addComponent(recipeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{recipeId}/components/{recipeComponentId}")
    public ResponseEntity<RecipeComponentResponseDto> updateComponent(@PathVariable("recipeId") Integer recipeId,
                                                                      @PathVariable("recipeComponentId") Integer recipeComponentId,
                                                                      @Valid @RequestBody RecipeComponentRequestDto request) {
        return ResponseEntity.ok(recipeService.updateComponent(recipeId, recipeComponentId, request));
    }

    @DeleteMapping("/{recipeId}/components/{recipeComponentId}")
    public ResponseEntity<Void> deleteComponent(@PathVariable("recipeId") Integer recipeId,
                                                @PathVariable("recipeComponentId") Integer recipeComponentId) {
        recipeService.deleteComponent(recipeId, recipeComponentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{recipeId}/steps")
    public ResponseEntity<RecipeStepResponseDto> addStep(@PathVariable("recipeId") Integer recipeId,
                                                         @Valid @RequestBody RecipeStepRequestDto request) {
        RecipeStepResponseDto created = recipeService.addStep(recipeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{recipeId}/steps/{recipeStepId}")
    public ResponseEntity<RecipeStepResponseDto> updateStep(@PathVariable("recipeId") Integer recipeId,
                                                            @PathVariable("recipeStepId") Integer recipeStepId,
                                                            @Valid @RequestBody RecipeStepRequestDto request) {
        return ResponseEntity.ok(recipeService.updateStep(recipeId, recipeStepId, request));
    }

    @DeleteMapping("/{recipeId}/steps/{recipeStepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable("recipeId") Integer recipeId,
                                           @PathVariable("recipeStepId") Integer recipeStepId) {
        recipeService.deleteStep(recipeId, recipeStepId);
        return ResponseEntity.noContent().build();
    }
}
