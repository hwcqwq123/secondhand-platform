package com.example.secondhand.order;

import java.time.Instant;

import com.example.secondhand.item.Item;
import com.example.secondhand.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders")
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /**
     * 原来如果是 @OneToOne(unique = true)，会导致：
     * 一个商品只能永远产生一条订单。
     *
     * 这样虽然能防止重复订单，但取消订单后也会影响再次下单。
     *
     * 现在改成 @ManyToOne：
     * 一个商品可以保留多条历史订单记录。
     * 真正防止并发重复下单由：
     * 1. Redis 锁；
     * 2. MySQL 行锁；
     * 3. 商品状态 AVAILABLE / RESERVED / SOLD；
     * 4. 后端事务；
     * 共同保证。
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public User getBuyer() {
        return buyer;
    }

    public Item getItem() {
        return item;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}