package com.aleitox.demo.controller;

import com.aleitox.demo.dto.DishDetailResponseDto;
import com.aleitox.demo.dto.DishRequestDto;
import com.aleitox.demo.dto.DishResponseDto;
import com.aleitox.demo.service.DishService;
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
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    public ResponseEntity<DishResponseDto> create(@Valid @RequestBody DishRequestDto request) {
        DishResponseDto created = dishService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DishResponseDto>> getAll() {
        return ResponseEntity.ok(dishService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishDetailResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(dishService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishResponseDto> update(@PathVariable Integer id,
                                                  @Valid @RequestBody DishRequestDto request) {
        return ResponseEntity.ok(dishService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        dishService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
