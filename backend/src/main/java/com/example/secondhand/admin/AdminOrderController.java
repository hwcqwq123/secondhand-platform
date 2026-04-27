package com.example.secondhand.admin;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemStatus;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.order.OrderResponse;
import com.example.secondhand.order.OrderStatus;
import com.example.secondhand.order.TradeOrder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderRepository orders;
    private final ItemRepository items;
    private final CurrentUser currentUser;
    private final AdminOperationLogService logService;

    public AdminOrderController(
            OrderRepository orders,
            ItemRepository items,
            CurrentUser currentUser,
            AdminOperationLogService logService
    ) {
        this.orders = orders;
        this.items = items;
        this.currentUser = currentUser;
        this.logService = logService;
    }

    /**
     * 后台订单查询：
     * 支持按商品标题、买家、卖家搜索，也支持订单状态筛选。
     */
    @GetMapping
    public ApiResponse<List<OrderResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) OrderStatus status
    ) {
        String keyword = q == null || q.isBlank() ? null : q.trim();

        List<OrderResponse> res = orders.findAdminOrders(keyword, status)
                .stream()
                .map(OrderResponse::new)
                .toList();

        return ApiResponse.ok(res);
    }

    /**
     * 管理员处理异常订单。
     * CREATED：商品锁定为 RESERVED
     * PAID：商品标记为 SOLD
     * CANCELED：订单取消；如果原来已支付，则商品改为 OFF_SHELF，避免再次售卖造成纠纷。
     */
    @PutMapping("/{id}/status")
    @Transactional
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateOrderStatusRequest req
    ) {
        String admin = currentUser.username();

        TradeOrder order = orders.findByIdWithItemBuyerSeller(id)
                .orElseThrow(() -> new EntityNotFoundException("order not found"));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = req.status();

        Item item = items.findWithLockById(order.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.CREATED) {
            item.setStatus(ItemStatus.RESERVED);
        } else if (newStatus == OrderStatus.PAID) {
            item.setStatus(ItemStatus.SOLD);
        } else if (newStatus == OrderStatus.CANCELED) {
            if (oldStatus == OrderStatus.PAID) {
                item.setStatus(ItemStatus.OFF_SHELF);
            } else {
                item.setStatus(ItemStatus.AVAILABLE);
            }
        }

        items.save(item);
        TradeOrder saved = orders.save(order);

        logService.record(
                admin,
                "UPDATE_ORDER_STATUS",
                "ORDER",
                id,
                "订单状态从 " + oldStatus + " 修改为 " + newStatus,
                req.safeReason()
        );

        return ApiResponse.ok(new OrderResponse(saved), "订单状态已更新");
    }
}