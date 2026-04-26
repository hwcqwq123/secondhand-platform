package com.example.secondhand.order;

import com.example.secondhand.item.ItemStatus;

import java.math.BigDecimal;

public class OrderResponse {
    private Long id;
    private String status;
    private String createdAt;
    private ItemInfo item;
    private BuyerInfo buyer;

    public OrderResponse() {
    }

    public OrderResponse(TradeOrder order) {
        this.id = order.getId();
        this.status = order.getStatus() != null ? order.getStatus().name() : null;
        this.createdAt = order.getCreatedAt() == null ? null : order.getCreatedAt().toString();

        // 订单里的商品信息
        if (order.getItem() != null) {
            this.item = new ItemInfo(order.getItem());
        }

        // 订单里的买家信息
        if (order.getBuyer() != null) {
            this.buyer = new BuyerInfo(order.getBuyer());
        }
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public ItemInfo getItem() {
        return item;
    }

    public BuyerInfo getBuyer() {
        return buyer;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setItem(ItemInfo item) {
        this.item = item;
    }

    public void setBuyer(BuyerInfo buyer) {
        this.buyer = buyer;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static class ItemInfo {
        private Long id;
        private String title;
        private BigDecimal price;
        private String status;
        private String coverImageUrl;
        private SellerInfo seller;

        public ItemInfo() {
        }

        public ItemInfo(com.example.secondhand.item.Item item) {
            this.id = item.getId();
            this.title = item.getTitle();
            this.price = item.getPrice();
            this.status = item.getStatus() != null ? item.getStatus().name() : null;
            this.coverImageUrl = item.getCoverImageUrl();

            if (item.getSeller() != null) {
                this.seller = new SellerInfo(item.getSeller());
            }
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getStatus() {
            return status;
        }

        public String getCoverImageUrl() {
            return coverImageUrl;
        }

        public SellerInfo getSeller() {
            return seller;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setCoverImageUrl(String coverImageUrl) {
            this.coverImageUrl = coverImageUrl;
        }

        public void setSeller(SellerInfo seller) {
            this.seller = seller;
        }
    }

    public static class SellerInfo {
        private Long id;
        private String username;
        private String nickname;
        private String wechatQrUrl;

        public SellerInfo() {
        }

        public SellerInfo(com.example.secondhand.user.User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.wechatQrUrl = user.getWechatQrUrl();
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getNickname() {
            return nickname;
        }

        public String getWechatQrUrl() {
            return wechatQrUrl;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public void setWechatQrUrl(String wechatQrUrl) {
            this.wechatQrUrl = wechatQrUrl;
        }
    }

    public static class BuyerInfo {
        private Long id;
        private String username;
        private String nickname;

        public BuyerInfo() {
        }

        public BuyerInfo(com.example.secondhand.user.User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.nickname = user.getNickname();
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getNickname() {
            return nickname;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}