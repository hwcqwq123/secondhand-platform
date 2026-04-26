package com.example.secondhand.reco;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.behavior.UserBehaviorRepository;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.*;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final CurrentUser currentUser;
    private final UserRepository users;
    private final UserBehaviorRepository behaviors;
    private final ItemRepository items;

    public RecommendationController(CurrentUser currentUser, UserRepository users,
                                    UserBehaviorRepository behaviors, ItemRepository items) {
        this.currentUser = currentUser;
        this.users = users;
        this.behaviors = behaviors;
        this.items = items;
    }

    @GetMapping
    public ApiResponse<List<ItemResponse>> recommend(@RequestParam(defaultValue = "10") int limit) {
        if (limit < 1) limit = 1;
        if (limit > 50) limit = 50;

        String username = currentUser.username();
        if (username == null) return ApiResponse.fail("unauthorized");

        User u = users.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("user not found"));

        // 1) 最近浏览 itemId
        List<Long> recentIds = behaviors.findRecentItemIds(u.getId(), "VIEW", PageRequest.of(0, 20));
        Set<Long> recentSet = new HashSet<>(recentIds);
        Set<Long> exclude = recentIds.stream().limit(2).collect(Collectors.toSet());

        // 2) 从最近浏览的商品标题提取关键词（MVP：简单切词）
        List<Item> recentItems = recentIds.isEmpty()
                ? List.of()
                : items.findAllById(recentIds);

        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        for (Item it : recentItems) {
            keywords.addAll(extractKeywords(it.getTitle(), 5));
            if (keywords.size() >= 8) break;
        }
        List<String> kwList = new ArrayList<>(keywords);

        // 3) 召回：对每个关键词用 searchWithSeller()（标题模糊匹配）合并去重
        LinkedHashMap<Long, Item> pool = new LinkedHashMap<>();
        for (String kw : kwList) {
            if (kw.isBlank()) continue;
            for (Item it : items.searchWithSeller(kw)) {
                if (pool.size() >= limit * 5) break;
                pool.putIfAbsent(it.getId(), it);
            }
            if (pool.size() >= limit * 3) break;
        }

        // 4) 过滤：排除最近看过的；排除 SOLD（你也可以选择保留 SOLD）
        List<Item> result = pool.values().stream()
                .filter(it -> !exclude.contains(it.getId()))
                .filter(it -> it.getStatus() != ItemStatus.SOLD)
                .filter(it -> !Boolean.TRUE.equals(it.getDeleted())) // 排除已删除的商品
                .filter(it -> it.getStatus() != ItemStatus.OFF_SHELF) // 排除下架商品
                .limit(limit)
                .collect(Collectors.toList());

        // 5) 不足补齐：最新商品
        if (result.size() < limit) {
            List<Item> latest = items.findLatestWithSeller(PageRequest.of(0, 50));
            for (Item it : latest) {
                if (result.size() >= limit) break;
                if (recentSet.contains(it.getId())) continue;
                if (it.getStatus() == ItemStatus.SOLD || Boolean.TRUE.equals(it.getDeleted())) continue;
                boolean exists = result.stream().anyMatch(x -> x.getId().equals(it.getId()));
                if (!exists) result.add(it);
            }
        }

        // 6) 映射 DTO
        List<ItemResponse> dto = result.stream().map(this::toDto).collect(Collectors.toList());
        return ApiResponse.ok(dto);
    }

    private ItemResponse toDto(Item i) {
        return new ItemResponse(
                i.getId(),
                i.getTitle(),
                i.getDescription(),
                i.getPrice(),
                i.getStatus(),
                i.getCreatedAt(),
                new ItemResponse.SellerResponse(i.getSeller().getId(), i.getSeller().getUsername()),
                i.getCoverImageUrl(),
                List.of(),
                i.getBoard()
        );
    }

    private static final Pattern NON_WORD = Pattern.compile("[^0-9A-Za-z\\u4e00-\\u9fa5]+");

    // MVP：按非中文/英文数字分隔，取前 N 个长度>=2的词
    private List<String> extractKeywords(String text, int max) {
        if (text == null) return List.of();
        String cleaned = NON_WORD.matcher(text).replaceAll(" ").trim();
        if (cleaned.isBlank()) return List.of();

        String[] parts = cleaned.split("\\s+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String w = p.trim();
            if (w.length() < 2) continue;
            out.add(w);
            if (out.size() >= max) break;
        }
        return out;
    }
}