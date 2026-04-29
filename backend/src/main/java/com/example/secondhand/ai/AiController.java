package com.example.secondhand.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.config.ApiResponse;

import jakarta.validation.Valid;

// 新增文件：AI 客服接口
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // 新增：真正的大模型客服问答接口
    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest req) {
        return ApiResponse.ok(aiService.chat(req));
    }
}
