package com.example.secondhand.admin;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final ItemRepository items;
    private final OrderRepository orders;
    private final CurrentUser currentUser;
    private final AdminOperationLogService logService;

    public AdminUserController(
            UserRepository users,
            ItemRepository items,
            OrderRepository orders,
            CurrentUser currentUser,
            AdminOperationLogService logService
    ) {
        this.users = users;
        this.items = items;
        this.orders = orders;
        this.currentUser = currentUser;
        this.logService = logService;
    }

    /**
     * 后台用户列表：
     * 支持用户名 / 昵称搜索，支持角色和状态筛选。
     */
    @GetMapping
    public ApiResponse<List<AdminUserResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status
    ) {
        String keyword = q == null || q.isBlank() ? null : q.trim();
        String finalRole = role == null || role.isBlank() ? null : AdminRole.normalize(role);

        List<AdminUserResponse> res = users.findAdminUsers(keyword, finalRole, status)
                .stream()
                .map(u -> AdminUserResponse.from(
                        u,
                        items.countBySeller_Id(u.getId()),
                        orders.countByBuyer_Id(u.getId()),
                        orders.countByItem_Seller_Id(u.getId())
                ))
                .toList();

        return ApiResponse.ok(res);
    }

    /**
     * 用户状态 / 角色管理：
     * 1. USER_ADMIN 可以封禁、解封用户；
     * 2. 只有 ADMIN / SUPER_ADMIN 可以分配管理员角色；
     * 3. 管理员不能封禁自己，也不能修改自己的角色。
     */
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest req
    ) {
        String adminUsername = currentUser.username();

        User admin = users.findByUsername(adminUsername)
                .orElseThrow(() -> new EntityNotFoundException("admin not found"));

        User target = users.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("user not found"));

        boolean isSelf = target.getUsername().equals(admin.getUsername());

        String oldRole = target.getRole();
        UserStatus oldStatus = target.getStatus();

        if (req.status() != null) {
            if (isSelf && req.status() == UserStatus.BANNED) {
                return ApiResponse.fail("不能封禁当前登录的管理员账号");
            }
            target.setStatus(req.status());
        }

        if (req.role() != null && !req.role().isBlank()) {
            if (isSelf) {
                return ApiResponse.fail("不能修改当前登录账号的角色");
            }

            if (!AdminRole.isSuperAdminLike(admin.getRole())) {
                return ApiResponse.fail("只有超级管理员可以分配角色");
            }

            String newRole = AdminRole.normalize(req.role());
            if (!AdminRole.isAssignable(newRole)) {
                return ApiResponse.fail("不支持的角色：" + req.role());
            }

            target.setRole(newRole);
        }

        User saved = users.save(target);

        logService.record(
                adminUsername,
                "UPDATE_USER",
                "USER",
                id,
                "用户状态从 " + oldStatus + " 改为 " + saved.getStatus()
                        + "，角色从 " + oldRole + " 改为 " + saved.getRole(),
                req.safeReason()
        );

        AdminUserResponse res = AdminUserResponse.from(
                saved,
                items.countBySeller_Id(saved.getId()),
                orders.countByBuyer_Id(saved.getId()),
                orders.countByItem_Seller_Id(saved.getId())
        );

        return ApiResponse.ok(res, "用户信息已更新");
    }
}