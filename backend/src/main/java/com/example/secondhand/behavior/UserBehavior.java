package com.example.secondhand.behavior;

import com.example.secondhand.user.User;
import com.example.secondhand.item.Item;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_behavior")
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, length = 16)
    private String type; // VIEW / CLICK / FAVORITE / ORDER

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Item getItem() { return item; }
    public String getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setItem(Item item) { this.item = item; }
    public void setType(String type) { this.type = type; }
}