package com.example.secondhand.user;

import jakarta.validation.constraints.Size;

public class UpdatePaymentRequest {

    @Size(max = 255, message = "收款码地址长度不能超过255个字符")
    private String wechatQrUrl;

    public String getWechatQrUrl() {
        return wechatQrUrl;
    }

    public void setWechatQrUrl(String wechatQrUrl) {
        this.wechatQrUrl = wechatQrUrl;
    }
}