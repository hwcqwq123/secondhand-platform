package com.example.secondhand.item;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_images")
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Item getItem() { return item; }
    public String getImageUrl() { return imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setItem(Item item) { this.item = item; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}