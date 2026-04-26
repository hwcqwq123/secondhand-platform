package com.example.secondhand.admin;

import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/items")
public class AdminItemController {

    private final ItemRepository items;

    public AdminItemController(ItemRepository items) {
        this.items = items;
    }

    /**
     * ✅ 管理员商品列表（带 seller 信息 + 支持筛选）
     * GET /api/admin/items?q=&status=
     */
    @GetMapping
    public ApiResponse<List<ItemResponse>> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) ItemStatus status) {

        List<Item> data;
        if (q != null && !q.isBlank()) {
            data = items.searchWithSeller(q);
        } else if (status != null) {
            data = items.findByStatusWithSeller(status);
        } else {
            data = items.findAllWithSeller();
        }

        List<ItemResponse> res = data.stream()
                .map(i -> new ItemResponse(
                        i.getId(),
                        i.getTitle(),
                        i.getDescription(),
                        i.getPrice(),
                        i.getStatus(),
                        i.getCreatedAt(),
                        new ItemResponse.SellerResponse(i.getSeller().getId(), i.getSeller().getUsername())
                ))
                .collect(Collectors.toList());

        return ApiResponse.ok(res);
    }

    /**
     * ✅ 管理员删除商品
     * DELETE /api/admin/items/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!items.existsById(id)) throw new EntityNotFoundException("item not found");
        items.deleteById(id);
        return ApiResponse.ok(null, "deleted");
    }

    /**
     * ✅ 管理员强制标记已售
     * PUT /api/admin/items/{id}/sold
     */
    @PutMapping("/{id}/sold")
    public ApiResponse<Item> markSold(@PathVariable Long id) {
        Item item = items.findById(id).orElseThrow(() -> new EntityNotFoundException("item not found"));
        item.setStatus(ItemStatus.SOLD);
        return ApiResponse.ok(items.save(item), "marked sold");
    }
}