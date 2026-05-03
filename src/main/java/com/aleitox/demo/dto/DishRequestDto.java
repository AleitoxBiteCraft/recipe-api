package com.aleitox.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DishRequestDto(
        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 5000)
        String description
) {
}
