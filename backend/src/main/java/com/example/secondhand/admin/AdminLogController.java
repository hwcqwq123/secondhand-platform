package com.example.secondhand.admin;

import com.example.secondhand.config.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logs")
public class AdminLogController {

    private final AdminOperationLogRepository logs;

    public AdminLogController(AdminOperationLogRepository logs) {
        this.logs = logs;
    }

    @GetMapping
    public ApiResponse<List<AdminOperationLogResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "200") int limit
    ) {
        String keyword = q == null || q.isBlank() ? null : q.trim();
        int safeLimit = Math.min(Math.max(limit, 1), 500);

        List<AdminOperationLogResponse> res = logs.search(keyword, PageRequest.of(0, safeLimit))
                .stream()
                .map(AdminOperationLogResponse::from)
                .toList();

        return ApiResponse.ok(res);
    }
}