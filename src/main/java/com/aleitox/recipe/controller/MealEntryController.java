package com.aleitox.recipe.controller;

import com.aleitox.recipe.dto.MealEntryDetailResponseDto;
import com.aleitox.recipe.dto.MealEntryResolvedResponseDto;
import com.aleitox.recipe.dto.MealEntryResponseDto;
import com.aleitox.recipe.service.MealEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
