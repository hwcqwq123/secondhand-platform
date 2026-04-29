package com.example.secondhand.ai;

import java.util.List;

import com.example.secondhand.item.ItemResponse;

// 新增文件：AI 客服返回对象
public record AiChatResponse(
        String answer,
        List<ItemResponse> suggestions,
        String provider,
        String model,
        Boolean llmEnabled,
        Boolean fallback
) {
}
