package com.example.secondhand.order;

import com.example.secondhand.item.Item;
import com.example.secondhand.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class TradeOrder {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private User buyer;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false, unique = true)
  private Item item;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private OrderStatus status = OrderStatus.CREATED;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public User getBuyer() { return buyer; }
  public Item getItem() { return item; }
  public OrderStatus getStatus() { return status; }

  public void setBuyer(User buyer) { this.buyer = buyer; }
  public void setItem(Item item) { this.item = item; }
  public void setStatus(OrderStatus status) { this.status = status; }
}
