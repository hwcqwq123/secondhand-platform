package com.example.secondhand.item;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findByItemIdOrderBySortOrderAsc(Long itemId);

    void deleteByItemId(Long itemId);

    boolean existsByImageUrl(String imageUrl);
}