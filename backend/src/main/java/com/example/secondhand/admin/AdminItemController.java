package com.example.secondhand.admin;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.*;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.order.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
public class AdminItemController {

    private final ItemRepository items;
    private final ItemImageRepository itemImages;
    private final OrderRepository orders;
    private final CurrentUser currentUser;
    private final AdminOperationLogService logService;

    public AdminItemController(
            ItemRepository items,
            ItemImageRepository itemImages,
            OrderRepository orders,
            CurrentUser currentUser,
            AdminOperationLogService logService
    ) {
        this.items = items;
        this.itemImages = itemImages;
        this.orders = orders;
        this.currentUser = currentUser;
        this.logService = logService;
    }

    /**
     * 管理员商品列表：
     * 展示卖家、发布时间、删除状态、订单关联状态等后台管理字段。
     */
    @GetMapping
    public ApiResponse<List<AdminItemResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        String keyword = q == null || q.isBlank() ? null : q.trim();

        List<AdminItemResponse> res = items.findAdminItems(keyword, status, includeDeleted)
                .stream()
                .map(item -> AdminItemResponse.from(
                        item,
                        orders.countByItem_Id(item.getId())
                ))
                .toList();

        return ApiResponse.ok(res);
    }

    /**
     * 管理员删除商品：
     * 1. 没有订单：允许物理删除；
     * 2. 有订单：不物理删除，改为逻辑删除 + 下架，避免订单详情丢失。
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReasonRequest req
    ) {
        String reason = req == null ? "未填写" : req.safeReason();
        String admin = currentUser.username();

        Item item = items.findByIdWithSeller(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("商品已经处于删除状态");
        }

        long orderCount = orders.countByItem_Id(id);

        if (orderCount > 0) {
            boolean hasUnfinishedOrder = orders.existsByItem_IdAndStatusIn(
                    id,
                    List.of(OrderStatus.CREATED)
            );

            item.setDeleted(true);
            item.setStatus(ItemStatus.OFF_SHELF);
            item.setDeleteCategory(
                    hasUnfinishedOrder
                            ? ItemDeleteCategory.ORDER_UNFINISHED
                            : ItemDeleteCategory.ORDER_FINISHED
            );

            items.save(item);

            logService.record(
                    admin,
                    "LOGIC_DELETE_ITEM",
                    "ITEM",
                    id,
                    "商品存在关联订单，执行逻辑删除并下架，订单数：" + orderCount,
                    reason
            );

            return ApiResponse.ok(null, "商品存在关联订单，已改为逻辑删除并下架");
        }

        itemImages.deleteByItemId(id);
        items.delete(item);

        logService.record(
                admin,
                "PHYSICAL_DELETE_ITEM",
                "ITEM",
                id,
                "商品无关联订单，执行物理删除",
                reason
        );

        return ApiResponse.ok(null, "商品无关联订单，已物理删除");
    }

    /**
     * 管理员强制下架商品。
     */
    @PutMapping("/{id}/off-shelf")
    @Transactional
    public ApiResponse<AdminItemResponse> offShelf(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReasonRequest req
    ) {
        String reason = req == null ? "未填写" : req.safeReason();
        String admin = currentUser.username();

        Item item = items.findByIdWithSeller(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("已删除商品不能重复下架");
        }

        item.setStatus(ItemStatus.OFF_SHELF);
        Item saved = items.save(item);

        logService.record(
                admin,
                "OFF_SHELF_ITEM",
                "ITEM",
                id,
                "管理员强制下架商品：" + item.getTitle(),
                reason
        );

        long orderCount = orders.countByItem_Id(id);
        return ApiResponse.ok(AdminItemResponse.from(saved, orderCount), "商品已下架");
    }

    /**
     * 管理员强制标记已售。
     */
    @PutMapping("/{id}/sold")
    @Transactional
    public ApiResponse<AdminItemResponse> markSold(
            @PathVariable Long id,
            @RequestBody(required = false) AdminReasonRequest req
    ) {
        String reason = req == null ? "未填写" : req.safeReason();
        String admin = currentUser.username();

        Item item = items.findByIdWithSeller(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("已删除商品不能标记已售");
        }

        item.setStatus(ItemStatus.SOLD);
        Item saved = items.save(item);

        logService.record(
                admin,
                "MARK_ITEM_SOLD",
                "ITEM",
                id,
                "管理员强制标记商品已售：" + item.getTitle(),
                reason
        );

        long orderCount = orders.countByItem_Id(id);
        return ApiResponse.ok(AdminItemResponse.from(saved, orderCount), "marked sold");
    }
}