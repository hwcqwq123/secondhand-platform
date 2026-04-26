package com.example.secondhand.item;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class ItemCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotBlank(message = "请选择板块")
    @Size(max = 32)
    private String board;

    @Size(max = 3, message = "最多上传3张图片")
    private List<String> imageUrls;

    private Integer coverIndex;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBoard() {
        return board;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Integer getCoverIndex() {
        return coverIndex;
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

    public void setBoard(String board) {
        this.board = board;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setCoverIndex(Integer coverIndex) {
        this.coverIndex = coverIndex;
    }
}