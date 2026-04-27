package com.example.secondhand.order;

import java.time.Duration;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RedisLockService redisLockService;
    private final CurrentUser currentUser;

    public OrderController(
            OrderService orderService,
            RedisLockService redisLockService,
            CurrentUser currentUser
    ) {
        this.orderService = orderService;
        this.redisLockService = redisLockService;
        this.currentUser = currentUser;
    }

    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> myOrders() {
        String username = currentUser.username();
        return orderService.myOrders(username);
    }

    @GetMapping("/my-sales")
    public ApiResponse<List<OrderResponse>> mySalesOrders() {
        String username = currentUser.username();
        return orderService.mySalesOrders(username);
    }

    /**
     * Redis 真正参与下单的位置。
     *
     * 流程：
     * 1. 根据商品 id 获取 Redis 锁 lock:item:{itemId}
     * 2. 获取失败，说明同一商品正在被其他用户下单
     * 3. 获取成功，进入 OrderService 的 MySQL 事务
     * 4. MySQL 事务中再次使用商品行锁兜底
     * 5. 事务结束后释放 Redis 锁
     */
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        String username = currentUser.username();

        String lockKey = "lock:item:" + req.itemId();
        String lockToken;

        try {
            lockToken = redisLockService.tryLock(lockKey, Duration.ofSeconds(10));
        } catch (Exception e) {
            return ApiResponse.fail("Redis 服务不可用，请稍后再试");
        }

        if (lockToken == null) {
            return ApiResponse.fail("该商品正在被其他用户下单，请稍后再试");
        }

        try {
            return orderService.createOrder(req.itemId(), username);
        } finally {
            redisLockService.unlock(lockKey, lockToken);
        }
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<OrderResponse> pay(@PathVariable Long id) {
        String username = currentUser.username();
        return orderService.payOrder(id, username);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        String username = currentUser.username();
        return orderService.cancelOrder(id, username);
    }
}