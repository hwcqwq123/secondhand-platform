package com.example.secondhand.order;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemStatus;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

    /**
     * 订单支付超时时间。
     */
    private static final long ORDER_EXPIRE_MINUTES = 30;

    private final OrderRepository orders;
    private final ItemRepository items;
    private final UserRepository users;

    public OrderService(
            OrderRepository orders,
            ItemRepository items,
            UserRepository users
    ) {
        this.orders = orders;
        this.items = items;
        this.users = users;
    }

    @Transactional
    public ApiResponse<List<OrderResponse>> myOrders(String username) {
        User buyer = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

        releaseExpiredOrders();

        List<OrderResponse> res = orders.findByBuyerIdWithItemSellerAndBuyer(buyer.getId())
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.ok(res);
    }

    @Transactional
    public ApiResponse<List<OrderResponse>> mySalesOrders(String username) {
        User seller = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("seller not found"));

        releaseExpiredOrders();

        List<OrderResponse> res = orders.findBySellerIdWithItemAndBuyer(seller.getId())
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());

        return ApiResponse.ok(res);
    }

    /**
     * 下单核心事务逻辑。
     *
     * Redis 锁已经在 Controller 层获取。
     * 这里继续使用 MySQL 行锁兜底，保证最终一致性。
     */
    @Transactional
    public ApiResponse<OrderResponse> createOrder(Long itemId, String username) {
        User buyer = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

        Item item = items.findWithLockById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        // 当前商品如果有超时未支付订单，先释放
        releaseExpiredOrdersForLockedItem(item);

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("该商品已被删除，无法下单");
        }

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            return ApiResponse.fail("该商品已被其他用户下单或已售出");
        }

        if (item.getSeller() != null && item.getSeller().getId().equals(buyer.getId())) {
            return ApiResponse.fail("不能购买自己发布的商品");
        }

        boolean hasActiveOrder = orders.existsActiveOrderByItemId(
                item.getId(),
                List.of(OrderStatus.CREATED, OrderStatus.PAID)
        );

        if (hasActiveOrder) {
            return ApiResponse.fail("该商品已有未完成订单或已售出");
        }

        TradeOrder order = new TradeOrder();
        order.setBuyer(buyer);
        order.setItem(item);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(Instant.now());

        item.setStatus(ItemStatus.RESERVED);

        items.save(item);
        TradeOrder saved = orders.save(order);

        return ApiResponse.ok(new OrderResponse(saved));
    }

    @Transactional
    public ApiResponse<OrderResponse> payOrder(Long id, String username) {
        User buyer = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

        TradeOrder order = orders.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            return ApiResponse.fail("only buyer can pay this order");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            return ApiResponse.fail("order is not payable");
        }

        Item item = items.findWithLockById(order.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (isOrderExpired(order)) {
            cancelExpiredOrderWithLockedItem(order, item);
            return ApiResponse.fail("订单已超时，商品已释放");
        }

        if (item.getStatus() != ItemStatus.RESERVED) {
            return ApiResponse.fail("item is not reserved");
        }

        item.setStatus(ItemStatus.SOLD);
        order.setStatus(OrderStatus.PAID);

        items.save(item);
        TradeOrder saved = orders.save(order);

        return ApiResponse.ok(new OrderResponse(saved));
    }

    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(Long id, String username) {
        User buyer = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

        TradeOrder order = orders.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            return ApiResponse.fail("only buyer can cancel this order");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            return ApiResponse.fail("order is not cancelable");
        }

        Item item = items.findWithLockById(order.getItem().getId())
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (item.getStatus() == ItemStatus.RESERVED) {
            item.setStatus(ItemStatus.AVAILABLE);
            items.save(item);
        }

        order.setStatus(OrderStatus.CANCELED);
        TradeOrder saved = orders.save(order);

        return ApiResponse.ok(new OrderResponse(saved));
    }

    private boolean isOrderExpired(TradeOrder order) {
        if (order.getCreatedAt() == null) {
            return false;
        }

        Instant deadline = order.getCreatedAt().plus(ORDER_EXPIRE_MINUTES, ChronoUnit.MINUTES);
        return Instant.now().isAfter(deadline);
    }

    /**
     * 释放当前商品的超时订单。
     * 注意：传入的 item 已经通过 MySQL 行锁锁住。
     */
    private void releaseExpiredOrdersForLockedItem(Item lockedItem) {
        Instant deadline = Instant.now().minus(ORDER_EXPIRE_MINUTES, ChronoUnit.MINUTES);

        List<TradeOrder> expiredOrders = orders.findExpiredCreatedOrdersByItemId(
                lockedItem.getId(),
                OrderStatus.CREATED,
                deadline
        );

        for (TradeOrder expiredOrder : expiredOrders) {
            cancelExpiredOrderWithLockedItem(expiredOrder, lockedItem);
        }
    }

    /**
     * 释放所有超时订单。
     * 查看订单列表时顺手清理。
     */
    private void releaseExpiredOrders() {
        Instant deadline = Instant.now().minus(ORDER_EXPIRE_MINUTES, ChronoUnit.MINUTES);

        List<TradeOrder> expiredOrders = orders.findByStatusAndCreatedAtBefore(
                OrderStatus.CREATED,
                deadline
        );

        for (TradeOrder order : expiredOrders) {
            Item item = items.findWithLockById(order.getItem().getId())
                    .orElseThrow(() -> new EntityNotFoundException("item not found"));

            cancelExpiredOrderWithLockedItem(order, item);
        }
    }

    /**
     * 取消超时订单，并释放商品。
     * 注意：传入的 item 必须已经被 MySQL 行锁锁住。
     */
    private void cancelExpiredOrderWithLockedItem(TradeOrder order, Item lockedItem) {
        if (order.getStatus() != OrderStatus.CREATED) {
            return;
        }

        if (lockedItem.getStatus() == ItemStatus.RESERVED) {
            lockedItem.setStatus(ItemStatus.AVAILABLE);
            items.save(lockedItem);
        }

        order.setStatus(OrderStatus.CANCELED);
        orders.save(order);
    }
}