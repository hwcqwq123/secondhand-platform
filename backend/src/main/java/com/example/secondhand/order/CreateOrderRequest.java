package com.example.secondhand.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@NotNull Long itemId) {}
