package com.example.secondhand.item;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.behavior.UserBehavior;
import com.example.secondhand.behavior.UserBehaviorRepository;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.order.OrderStatus;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemRepository items;
    private final ItemImageRepository itemImages;
    private final UserRepository users;
    private final CurrentUser currentUser;
    private final UserBehaviorRepository behaviors;
    private final OrderRepository orders;

    public ItemController(
            ItemRepository items,
            ItemImageRepository itemImages,
            UserRepository users,
            CurrentUser currentUser,
            UserBehaviorRepository behaviors,
            OrderRepository orders
    ) {
        this.items = items;
        this.itemImages = itemImages;
        this.users = users;
        this.currentUser = currentUser;
        this.behaviors = behaviors;
        this.orders = orders;
    }

    @GetMapping
    public ApiResponse<List<ItemResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ItemStatus status,
            @RequestParam(required = false) String board
    ) {
        List<Item> data;

        if (q != null && !q.isBlank()) {
            data = items.searchWithSeller(q);
        } else if (status != null) {
            data = items.findByStatusWithSeller(status);
        } else {
            data = items.findAllWithSeller();
        }

        data = data.stream()
                .filter(i -> !Boolean.TRUE.equals(i.getDeleted()))
                .filter(i -> i.getStatus() != ItemStatus.OFF_SHELF)
                .collect(Collectors.toList());

        if (board != null && !board.isBlank() && !"all".equalsIgnoreCase(board)) {
            data = data.stream()
                    .filter(i -> board.equals(i.getBoard()))
                    .collect(Collectors.toList());
        }

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
                        u.getId(),
                        i.getId(),
                        "VIEW",
                        cutoff
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
        item.setStatus(ItemStatus.AVAILABLE);
        item.setDeleted(false);
        item.setDeleteCategory(ItemDeleteCategory.NONE);

        List<String> imageUrls = req.getImageUrls();
        Integer coverIndex = req.getCoverIndex();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int finalCoverIndex = coverIndex == null ? 0 : coverIndex;

            if (finalCoverIndex < 0 || finalCoverIndex >= imageUrls.size()) {
                finalCoverIndex = 0;
            }

            item.setCoverImageUrl(imageUrls.get(finalCoverIndex));
        }

        Item saved = items.save(item);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (int index = 0; index < imageUrls.size(); index++) {
                String url = imageUrls.get(index);

                if (url == null || url.isBlank()) {
                    continue;
                }

                ItemImage image = new ItemImage();
                image.setItem(saved);
                image.setImageUrl(url);
                image.setSortOrder(index);
                itemImages.save(image);
            }
        }

        return ApiResponse.ok(toResponseWithImages(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<ItemResponse> update(@PathVariable Long id, @Valid @RequestBody ItemEditRequest req) {
        String username = currentUser.username();

        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (!canManageItem(item, username)) {
            return ApiResponse.fail("only owner/admin can update");
        }

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("已删除商品不能修改");
        }

        if (item.getStatus() == ItemStatus.RESERVED) {
            return ApiResponse.fail("商品存在未完成订单，暂不能修改");
        }

        if (item.getStatus() == ItemStatus.SOLD) {
            return ApiResponse.fail("已售出商品不能修改");
        }

        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setBoard(req.getBoard());

        List<String> imageUrls = req.getImageUrls();
        Integer coverIndex = req.getCoverIndex();

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int finalCoverIndex = coverIndex == null ? 0 : coverIndex;

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
            for (int index = 0; index < imageUrls.size(); index++) {
                String url = imageUrls.get(index);

                if (url == null || url.isBlank()) {
                    continue;
                }

                ItemImage image = new ItemImage();
                image.setItem(saved);
                image.setImageUrl(url);
                image.setSortOrder(index);
                itemImages.save(image);
            }
        }

        return ApiResponse.ok(toResponseWithImages(saved), "updated");
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ApiResponse<List<ItemResponse>> myItems() {
        String username = currentUser.username();

        User seller = users.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("seller not found"));

        List<Item> data = items.findBySellerIdWithSeller(seller.getId()).stream()
                .filter(item -> !Boolean.TRUE.equals(item.getDeleted()))
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

        if (!canManageItem(item, username)) {
            return ApiResponse.fail("only owner/admin can off-shelf this item");
        }

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("已删除商品不能下架");
        }

        if (item.getStatus() == ItemStatus.OFF_SHELF) {
            return ApiResponse.fail("item already off-shelf");
        }

        if (item.getStatus() == ItemStatus.RESERVED) {
            return ApiResponse.fail("商品存在未完成订单，不能下架");
        }

        if (item.getStatus() == ItemStatus.SOLD) {
            return ApiResponse.fail("已售出商品不能下架");
        }

        item.setStatus(ItemStatus.OFF_SHELF);
        Item saved = items.save(item);

        return ApiResponse.ok(toResponseWithImages(saved), "off-shelf success");
    }

    @PutMapping("/{id}/put-on-shelf")
    @Transactional
    public ApiResponse<ItemResponse> putOnShelf(@PathVariable Long id) {
        String username = currentUser.username();

        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (!canManageItem(item, username)) {
            return ApiResponse.fail("only owner/admin can put on shelf this item");
        }

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("已删除商品不能重新上架");
        }

        if (item.getStatus() != ItemStatus.OFF_SHELF) {
            return ApiResponse.fail("only off-shelf item can be put on shelf");
        }

        boolean hasActiveOrder = orders.existsByItem_IdAndStatusIn(
                item.getId(),
                List.of(OrderStatus.CREATED, OrderStatus.PAID)
        );

        if (hasActiveOrder) {
            return ApiResponse.fail("商品存在订单，不能重新上架");
        }

        item.setStatus(ItemStatus.AVAILABLE);
        item.setDeleteCategory(ItemDeleteCategory.NONE);

        Item saved = items.save(item);

        return ApiResponse.ok(toResponseWithImages(saved), "put on shelf success");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id) {
        String username = currentUser.username();

        Item item = items.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("item not found"));

        if (!canManageItem(item, username)) {
            return ApiResponse.fail("only owner/admin can delete");
        }

        if (Boolean.TRUE.equals(item.getDeleted())) {
            return ApiResponse.fail("商品已经删除");
        }

        boolean hasUnfinishedOrder = orders.existsByItem_IdAndStatus(
                item.getId(),
                OrderStatus.CREATED
        );

        if (hasUnfinishedOrder) {
            item.setDeleteCategory(ItemDeleteCategory.ORDER_UNFINISHED);
            items.save(item);
            return ApiResponse.fail("商品存在未完成订单，不能删除");
        }

        boolean hasPaidOrder = orders.existsByItem_IdAndStatus(
                item.getId(),
                OrderStatus.PAID
        );

        boolean hasAnyOrder = orders.existsByItem_Id(item.getId());

        item.setDeleted(true);

        if (hasPaidOrder) {
            item.setDeleteCategory(ItemDeleteCategory.ORDER_FINISHED);
            item.setStatus(ItemStatus.SOLD);
        } else if (hasAnyOrder) {
            item.setDeleteCategory(ItemDeleteCategory.ORDER_UNFINISHED);
            item.setStatus(ItemStatus.OFF_SHELF);
        } else {
            item.setDeleteCategory(ItemDeleteCategory.NO_ORDER);
            item.setStatus(ItemStatus.OFF_SHELF);
        }

        items.save(item);

        // 关键修改：
        // 不再删除 item_images。
        // 原因：订单历史、后台审计、商品记录可能仍然需要商品图片。
        // 图片文件清理后续应通过“未引用图片清理任务”单独处理。
        return ApiResponse.ok(null, "item marked as deleted");
    }

    private boolean canManageItem(Item item, String username) {
        boolean isOwner = item.getSeller().getUsername().equals(username);

        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isOwner || isAdmin;
    }

    private ItemResponse toResponseWithImages(Item item) {
        List<String> imageUrls = itemImages.findByItemIdOrderBySortOrderAsc(item.getId())
                .stream()
                .map(ItemImage::getImageUrl)
                .collect(Collectors.toList());

        return new ItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice(),
                item.getStatus(),
                item.getCreatedAt(),
                new ItemResponse.SellerResponse(
                        item.getSeller().getId(),
                        item.getSeller().getUsername(),
                        item.getSeller().getNickname()
                ),
                item.getCoverImageUrl(),
                imageUrls,
                item.getBoard()
        );
    }
}