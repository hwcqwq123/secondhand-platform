package com.example.secondhand.admin;

import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemStatus;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.order.OrderStatus;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final UserRepository users;
    private final ItemRepository items;
    private final OrderRepository orders;

    public AdminStatsController(
            UserRepository users,
            ItemRepository items,
            OrderRepository orders
    ) {
        this.users = users;
        this.items = items;
        this.orders = orders;
    }

    @GetMapping
    public ApiResponse<AdminStatsResponse> stats() {
        AdminStatsResponse res = new AdminStatsResponse(
                users.count(),
                users.countByStatus(UserStatus.BANNED),

                items.count(),
                items.countByStatus(ItemStatus.AVAILABLE),
                items.countByStatus(ItemStatus.RESERVED),
                items.countByStatus(ItemStatus.SOLD),
                items.countByStatus(ItemStatus.OFF_SHELF),
                items.countByDeleted(true),

                orders.count(),
                orders.countByStatus(OrderStatus.CREATED),
                orders.countByStatus(OrderStatus.PAID),
                orders.countByStatus(OrderStatus.CANCELED)
        );

        return ApiResponse.ok(res);
    }
}