package com.aleitox.recipe.controller;

import com.aleitox.recipe.dto.MealEntryDetailResponseDto;
import com.aleitox.recipe.dto.MealEntryRequestDto;
import com.aleitox.recipe.dto.MealEntryResolvedResponseDto;
import com.aleitox.recipe.dto.MealEntryResponseDto;
import com.aleitox.recipe.service.MealEntryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meal-entries")
public class MealEntryController {

    private final MealEntryService mealEntryService;

    public MealEntryController(MealEntryService mealEntryService) {
        this.mealEntryService = mealEntryService;
    }

    @GetMapping
    public ResponseEntity<List<MealEntryResponseDto>> getAll() {
        return ResponseEntity.ok(mealEntryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealEntryDetailResponseDto> getById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(mealEntryService.getById(id));
    }

    @GetMapping("/{id}/resolved")
    public ResponseEntity<MealEntryResolvedResponseDto> getResolvedById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(mealEntryService.getResolvedById(id));
    }

    @PostMapping
    public ResponseEntity<MealEntryDetailResponseDto> create(@Valid @RequestBody MealEntryRequestDto request) {
        MealEntryDetailResponseDto created = mealEntryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        mealEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
