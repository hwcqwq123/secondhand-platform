package com.example.secondhand.order;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.Item;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.item.ItemStatus;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderRepository orders;
  private final ItemRepository items;
  private final UserRepository users;
  private final CurrentUser currentUser;

  public OrderController(OrderRepository orders, ItemRepository items, UserRepository users, CurrentUser currentUser) {
    this.orders = orders;
    this.items = items;
    this.users = users;
    this.currentUser = currentUser;
  }

  @GetMapping("/my")
  @Transactional(readOnly = true)
  public ApiResponse<List<OrderResponse>> myOrders() {
    String username = currentUser.username();
    User buyer = users.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

    releaseExpiredOrders();

    List<OrderResponse> res = orders.findByBuyerIdWithItemSellerAndBuyer(buyer.getId())
            .stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());

    return ApiResponse.ok(res);
  }

  @GetMapping("/my-sales")
  @Transactional(readOnly = true)
  public ApiResponse<List<OrderResponse>> mySalesOrders() {
    String username = currentUser.username();
    User seller = users.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("seller not found"));

    releaseExpiredOrders();

    List<OrderResponse> res = orders.findBySellerIdWithItemAndBuyer(seller.getId())
            .stream()
            .map(OrderResponse::new)
            .collect(Collectors.toList());

    return ApiResponse.ok(res);
  }

  @PostMapping
  @Transactional
  public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
    String username = currentUser.username();
    User buyer = users.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

    Item item = items.findWithLockById(req.itemId())
            .orElseThrow(() -> new EntityNotFoundException("item not found"));

    if (item.getStatus() != ItemStatus.AVAILABLE) {
      return ApiResponse.fail("item is not available");
    }

    item.setStatus(ItemStatus.RESERVED);
    items.save(item);

    TradeOrder o = new TradeOrder();
    o.setBuyer(buyer);
    o.setItem(item);
    o.setStatus(OrderStatus.CREATED);

    TradeOrder saved = orders.save(o);
    return ApiResponse.ok(new OrderResponse(saved));
  }

  @PutMapping("/{id}/pay")
  @Transactional
  public ApiResponse<OrderResponse> pay(@PathVariable Long id) {
    String username = currentUser.username();
    User buyer = users.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

    TradeOrder o = orders.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("order not found"));

    if (!o.getBuyer().getId().equals(buyer.getId())) {
      return ApiResponse.fail("only buyer can pay this order");
    }

    if (o.getStatus() != OrderStatus.CREATED) {
      return ApiResponse.fail("order is not payable");
    }


    // 先检查是否已经超时
    if (isOrderExpired(o)) {
      cancelExpiredOrder(o);
      return ApiResponse.fail("order expired, item released");
    }

    Item item = items.findWithLockById(o.getItem().getId())
            .orElseThrow(() -> new EntityNotFoundException("item not found"));

    if (item.getStatus() != ItemStatus.RESERVED) {
      return ApiResponse.fail("item is not reserved");
    }

    item.setStatus(ItemStatus.SOLD);
    items.save(item);

    o.setStatus(OrderStatus.PAID);
    TradeOrder saved = orders.save(o);

    return ApiResponse.ok(new OrderResponse(saved));
  }

  @PutMapping("/{id}/cancel")
  @Transactional
  public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
    String username = currentUser.username();
    User buyer = users.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("buyer not found"));

    TradeOrder o = orders.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("order not found"));

    if (!o.getBuyer().getId().equals(buyer.getId())) {
      return ApiResponse.fail("only buyer can cancel this order");
    }

    if (o.getStatus() != OrderStatus.CREATED) {
      return ApiResponse.fail("order is not cancelable");
    }

    Item item = items.findWithLockById(o.getItem().getId())
            .orElseThrow(() -> new EntityNotFoundException("item not found"));

    if (item.getStatus() == ItemStatus.RESERVED) {
      item.setStatus(ItemStatus.AVAILABLE);
      items.save(item);
    }

    o.setStatus(OrderStatus.CANCELED);
    TradeOrder saved = orders.save(o);

    return ApiResponse.ok(new OrderResponse(saved));
  }

  private boolean isOrderExpired(TradeOrder order) {
    if (order.getCreatedAt() == null) return false;
    Instant deadline = order.getCreatedAt().plus(30, ChronoUnit.MINUTES);
    return Instant.now().isAfter(deadline);
  }

  private void cancelExpiredOrder(TradeOrder order) {
    Item item = items.findWithLockById(order.getItem().getId())
            .orElseThrow(() -> new EntityNotFoundException("item not found"));

    if (item.getStatus() == ItemStatus.RESERVED) {
      item.setStatus(ItemStatus.AVAILABLE);
      items.save(item);
    }

    order.setStatus(OrderStatus.CANCELED);
    orders.save(order);
  }

  private void releaseExpiredOrders() {
    Instant deadline = Instant.now().minus(30, ChronoUnit.MINUTES);
    List<TradeOrder> expiredOrders = orders.findByStatusAndCreatedAtBefore(OrderStatus.CREATED, deadline);

    for (TradeOrder order : expiredOrders) {
      cancelExpiredOrder(order);
    }
  }
}