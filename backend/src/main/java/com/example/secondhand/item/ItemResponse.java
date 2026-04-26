package com.example.secondhand.item;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ItemResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        ItemStatus status,
        Instant createdAt,
        SellerResponse seller,
        String coverImageUrl,
        List<String> imageUrls,
        String board
) {

    public ItemResponse(
            Long id,
            String title,
            String description,
            BigDecimal price,
            ItemStatus status,
            Instant createdAt,
            SellerResponse seller
    ) {
        this(id, title, description, price, status, createdAt, seller, null, List.of(), null);
    }

    public ItemResponse(
            Long id,
            String title,
            String description,
            BigDecimal price,
            ItemStatus status,
            Instant createdAt,
            SellerResponse seller,
            String coverImageUrl
    ) {
        this(id, title, description, price, status, createdAt, seller, coverImageUrl, List.of(), null);
    }

    public ItemResponse(
            Long id,
            String title,
            String description,
            BigDecimal price,
            ItemStatus status,
            Instant createdAt,
            SellerResponse seller,
            String coverImageUrl,
            List<String> imageUrls
    ) {
        this(id, title, description, price, status, createdAt, seller, coverImageUrl, imageUrls, null);
    }

    public record SellerResponse(
            Long id,
            String username,
            String nickname
    ) {
        public SellerResponse(Long id, String username) {
            this(id, username, null);
        }
    }
}