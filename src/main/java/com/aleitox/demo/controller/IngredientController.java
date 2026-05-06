package com.aleitox.demo.controller;

import com.aleitox.demo.dto.IngredientRequestDto;
import com.aleitox.demo.dto.IngredientResponseDto;
import com.aleitox.demo.service.IngredientService;
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
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @PostMapping
    public ResponseEntity<IngredientResponseDto> create(@Valid @RequestBody IngredientRequestDto request) {
        IngredientResponseDto created = ingredientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponseDto>> getAll() {
        return ResponseEntity.ok(ingredientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponseDto> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(ingredientService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientResponseDto> update(@PathVariable("id") Integer id,
                                                        @Valid @RequestBody IngredientRequestDto request) {
        return ResponseEntity.ok(ingredientService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        ingredientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
