package com.example.secondhand.item;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.behavior.UserBehavior;
import com.example.secondhand.behavior.UserBehaviorRepository;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository items;
    private final ItemImageRepository itemImages;
    private final UserRepository users;
    private final CurrentUser currentUser;
    private final UserBehaviorRepository behaviors;

    public ItemController(
            ItemRepository items,
            ItemImageRepository itemImages,
            UserRepository users,
            CurrentUser currentUser,
            UserBehaviorRepository behaviors
    ) {
        this.items = items;
        this.itemImages = itemImages;
        this.users = users;
        this.currentUser = currentUser;
        this.behaviors = behaviors;
    }

    @GetMapping
    public ApiResponse<List<ItemResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String board
    ) {
        List<Item> data;

        // 根据搜索条件加载商品
        if (q != null && !q.isBlank()) {
            data = items.searchWithSeller(q);
        } else if (status != null) {
            // 根据状态加载商品
            data = items.findByStatusWithSeller(status);
        } else {
            // 默认加载所有商品
            data = items.findAllWithSeller();
        }

        // 默认隐藏下架商品和已删除商品
        data = data.stream()
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))  // 过滤已删除商品
                .filter(i -> i.getStatus() != ItemStatus.OFF_SHELF) // 过滤下架商品
                .collect(Collectors.toList());

        // 按板块过滤商品，若board为空或"all"，则不过滤
        if (board != null && !board.isBlank() && !"all".equalsIgnoreCase(board)) {
            data = data.stream()
                    .filter(i -> board.equals(i.getBoard()))  // 根据板块进行过滤
                    .collect(Collectors.toList());
        }

        // 将筛选后的数据转换为ItemResponse对象
        List<ItemResponse> res = data.stream()
                .map(i -> new ItemResponse(
                        i.getId(),
                        i.getTitle(),
                        i.getDescription(),
                        i.getPrice(),
                        i.getStatus(),
                        i.getCreatedAt(),
                        new ItemResponse.SellerResponse(
                                i.getSeller().getId(),
                                i.getSeller().getUsername(),
                                i.getSeller().getNickname()
                        ),
                        i.getCoverImageUrl(),
                        List.of(),
                        i.getBoard()  // 添加板块信息
                ))
                .collect(Collectors.toList());

        return ApiResponse.ok(res);
    }

    @Transactional
    @GetMapping("/{id}")
    public ApiResponse<ItemResponse> detail(@PathVariable Long id) {
        Item i = items.findByIdWithSeller(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        String username = currentUser.username();
        if (username != null) {
            try {
                User u = users.findByUsername(username)
                        .orElseThrow(() -> new EntityNotFoundException("user not found"));

                Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);
                boolean exists = behaviors.existsByUser_IdAndItem_IdAndTypeAndCreatedAtAfter(
                        u.getId(), i.getId(), "VIEW", cutoff
                );

                if (!exists) {
                    UserBehavior b = new UserBehavior();
                    b.setUser(u);
                    b.setItem(i);
                    b.setType("VIEW");
                    behaviors.save(b);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> imageUrls = itemImages.findByItemIdOrderBySortOrderAsc(i.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        ItemResponse res = new ItemResponse(
                i.getId(),
                i.getTitle(),
                i.getDescription(),
                i.getPrice(),
                i.getStatus(),
                i.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        i.getSeller().getId(),
                        i.getSeller().getUsername(),
                        i.getSeller().getNickname()
                ),
                i.getCoverImageUrl(),
                imageUrls,
                i.getBoard()
        );
        return ApiResponse.ok(res);
    }

    @PostMapping
    @Transactional
    public ApiResponse<ItemResponse> create(@Valid @RequestBody ItemCreateRequest req) {
        String username = currentUser.username();
        User seller = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("seller not found"));

        Item item = new Item();
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setSeller(seller);
        item.setBoard(req.getBoard());

        List<String> imageUrls = req.getImageUrls();
        Integer coverIndex = req.getCoverIndex();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int finalCoverIndex = (coverIndex == null ? 0 : coverIndex);
            if (finalCoverIndex < 0 || finalCoverIndex >= imageUrls.size()) {
                finalCoverIndex = 0;
            }
            item.setCoverImageUrl(imageUrls.get(finalCoverIndex));
        }

        Item saved = items.save(item);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                if (url == null || url.isBlank()) continue;

                ItemImage image = new ItemImage();
                image.setItem(saved);
                image.setImageUrl(url);
                image.setSortOrder(i);
                itemImages.save(image);
            }
        }

        List<String> savedImageUrls = itemImages.findByItemIdOrderBySortOrderAsc(saved.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        ItemResponse res = new ItemResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        seller.getId(),
                        seller.getUsername(),
                        seller.getNickname()
                ),
                saved.getCoverImageUrl(),
                savedImageUrls,
                saved.getBoard()
        );

        return ApiResponse.ok(res);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<ItemResponse> update(@PathVariable Long id, @Valid @RequestBody ItemEditRequest req) {
        String username = currentUser.username();
        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        boolean isOwner = item.getSeller().getUsername().equals(username);
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ApiResponse.fail("only owner/admin can update");
        }

        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setBoard(req.getBoard());

        List<String> imageUrls = req.getImageUrls();
        Integer coverIndex = req.getCoverIndex();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int finalCoverIndex = (coverIndex == null ? 0 : coverIndex);
            if (finalCoverIndex < 0 || finalCoverIndex >= imageUrls.size()) {
                finalCoverIndex = 0;
            }
            item.setCoverImageUrl(imageUrls.get(finalCoverIndex));
        } else {
            item.setCoverImageUrl(null);
        }

        Item saved = items.save(item);

        itemImages.deleteByItemId(saved.getId());

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int i = 0; i < imageUrls.size(); i++) {
                String url = imageUrls.get(i);
                if (url == null || url.isBlank()) continue;

                ItemImage image = new ItemImage();
                image.setItem(saved);
                image.setImageUrl(url);
                image.setSortOrder(i);
                itemImages.save(image);
            }
        }

        List<String> savedImageUrls = itemImages.findByItemIdOrderBySortOrderAsc(saved.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        ItemResponse res = new ItemResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        saved.getSeller().getId(),
                        saved.getSeller().getUsername(),
                        saved.getSeller().getNickname()
                ),
                saved.getCoverImageUrl(),
                savedImageUrls,
                saved.getBoard()
        );

        return ApiResponse.ok(res, "updated");
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ApiResponse<List<ItemResponse>> myItems() {
        String username = currentUser.username();
        User seller = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("seller not found"));

        // 仅查询未删除且为卖家发布的商品
        List<Item> data = items.findBySellerIdWithSeller(seller.getId()).stream()
                .filter(item -> !item.getDeleted()) // 排除已删除商品
                .collect(Collectors.toList());

        List<ItemResponse> res = data.stream()
                .map(i -> new ItemResponse(
                        i.getId(),
                        i.getTitle(),
                        i.getDescription(),
                        i.getPrice(),
                        i.getStatus(),
                        i.getCreatedAt(),
                        new ItemResponse.SellerResponse(
                                i.getSeller().getId(),
                                i.getSeller().getUsername(),
                                i.getSeller().getNickname()
                        ),
                        i.getCoverImageUrl(),
                        List.of(),
                        i.getBoard()
                ))
                .collect(Collectors.toList());

        return ApiResponse.ok(res);
    }


    @PutMapping("/{id}/off-shelf")
    @Transactional
    public ApiResponse<ItemResponse> offShelf(@PathVariable Long id) {
        String username = currentUser.username();
        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        boolean isOwner = item.getSeller().getUsername().equals(username);
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ApiResponse.fail("only owner/admin can off-shelf this item");
        }

        if (item.getStatus() == ItemStatus.OFF_SHELF) {
            return ApiResponse.fail("item already off-shelf");
        }

        item.setStatus(ItemStatus.OFF_SHELF);
        Item saved = items.save(item);

        List<String> imageUrls = itemImages.findByItemIdOrderBySortOrderAsc(saved.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        ItemResponse res = new ItemResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        saved.getSeller().getId(),
                        saved.getSeller().getUsername(),
                        saved.getSeller().getNickname()
                ),
                saved.getCoverImageUrl(),
                imageUrls,
                saved.getBoard()
        );

        return ApiResponse.ok(res, "off-shelf success");
    }

    @PutMapping("/{id}/put-on-shelf")
    @Transactional
    public ApiResponse<ItemResponse> putOnShelf(@PathVariable Long id) {
        String username = currentUser.username();
        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        boolean isOwner = item.getSeller().getUsername().equals(username);
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ApiResponse.fail("only owner/admin can put on shelf this item");
        }

        if (item.getStatus() != ItemStatus.OFF_SHELF) {
            return ApiResponse.fail("only off-shelf item can be put on shelf");
        }

        item.setStatus(ItemStatus.AVAILABLE);
        Item saved = items.save(item);

        List<String> imageUrls = itemImages.findByItemIdOrderBySortOrderAsc(saved.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        ItemResponse res = new ItemResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        saved.getSeller().getId(),
                        saved.getSeller().getUsername(),
                        saved.getSeller().getNickname()
                ),
                saved.getCoverImageUrl(),
                imageUrls,
                saved.getBoard()
        );

        return ApiResponse.ok(res, "put on shelf success");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = currentUser.username();
        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        boolean isOwner = item.getSeller().getUsername().equals(username);
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            return ApiResponse.fail("only owner/admin can delete");
        }

        // 标记商品为已删除并下架
        item.setDeleted(true);  // 标记删除
        item.setStatus(ItemStatus.OFF_SHELF);  // 也可以设置为已下架
        items.save(item);  // 保存更新的商品

        // 删除该商品相关的图片
        itemImages.deleteByItemId(item.getId());

        return ApiResponse.ok(null, "item marked as deleted");
    }
}