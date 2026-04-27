package com.example.secondhand.admin;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemDeleteCategory;
import com.example.secondhand.item.ItemStatus;

public record AdminItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        ItemStatus status,
        Instant createdAt,
        String board,
        String coverImageUrl,
        Boolean deleted,
        ItemDeleteCategory deleteCategory,
        SellerInfo seller,
        Boolean hasOrder,
        Long orderCount
) {
    public static AdminItemResponse from(Item item, long orderCount) {
        return new AdminItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getBoard(),
                item.getCoverImageUrl(),
                Boolean.TRUE.equals(item.getDeleted()),
                item.getDeleteCategory(),
                item.getSeller() == null ? null : new SellerInfo(
                        item.getSeller().getId(),
                        item.getSeller().getUsername(),
                        item.getSeller().getNickname()
                ),
                orderCount > 0,
                orderCount
        );
    }

    public record SellerInfo(
            Long id,
            String username,
            String nickname
    ) {}
}