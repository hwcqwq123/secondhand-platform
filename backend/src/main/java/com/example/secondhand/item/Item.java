package com.example.secondhand.item;

import com.example.secondhand.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "items")
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false, length = 120)
  private String title;

  @Column(length = 2000)
  private String description;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private User seller;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ItemStatus status = ItemStatus.AVAILABLE;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "cover_image_url", length = 255)
  private String coverImageUrl;  // 用来存储封面图 URL

  @Column(name = "board", nullable = false, length = 32)
  private String board = "daily";  // 商品所在的板块

  @Column(nullable = false)
  private Boolean deleted = false;


  @Enumerated(EnumType.STRING)
  @Column(name = "delete_category", nullable = false, length = 32)
  private ItemDeleteCategory deleteCategory = ItemDeleteCategory.NONE;


  @ElementCollection
  @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
  @Column(name = "image_url")
  private List<String> imageUrls;  // 用来存储商品的多张图片 URL

  @Version
  private Long version;

  // Getter 和 Setter 方法
  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public User getSeller() {
    return seller;
  }

  public ItemStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public String getBoard() {
    return board;
  }

  public List<String> getImageUrls() {
    return imageUrls;
  }

  public Long getVersion() {
    return version;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public ItemDeleteCategory getDeleteCategory() {
    return deleteCategory;
  }

  public void setDeleteCategory(ItemDeleteCategory deleteCategory) {
    this.deleteCategory = deleteCategory;
  }


  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public void setSeller(User seller) {
    this.seller = seller;
  }

  public void setStatus(ItemStatus status) {
    this.status = status;
  }

  public void setCoverImageUrl(String coverImageUrl) {
    this.coverImageUrl = coverImageUrl;
  }

  public void setBoard(String board) {
    this.board = board;
  }

  public void setImageUrls(List<String> imageUrls) {
    this.imageUrls = imageUrls;
  }
}

