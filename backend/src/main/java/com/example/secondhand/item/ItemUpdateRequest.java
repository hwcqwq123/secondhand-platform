package com.example.secondhand.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemUpdateRequest(
        @NotBlank String title,
        String description,
        @NotNull @Min(0) BigDecimal price
) {}