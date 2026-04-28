package com.example.secondhand.admin;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.secondhand.auth.CurrentUser;
import com.example.secondhand.config.ApiResponse;
import com.example.secondhand.item.ItemRepository;
import com.example.secondhand.order.OrderRepository;
import com.example.secondhand.user.User;
import com.example.secondhand.user.UserRepository;
import com.example.secondhand.user.UserStatus;

import jakarta.persistence.EntityNotFoundException;

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
     * 2. 只有固定超级管理员 admin 可以分配角色；
     * 3. 超级管理员只能是唯一固定账号 admin；
     * 4. 管理员不能封禁自己，也不能修改自己的角色。
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
        boolean targetIsFixedSuperAdmin = "admin".equalsIgnoreCase(target.getUsername());

        String oldRole = target.getRole();
        UserStatus oldStatus = target.getStatus();

        if (req.status() != null) {
            if (isSelf && req.status() == UserStatus.BANNED) {
                return ApiResponse.fail("不能封禁当前登录的管理员账号");
            }
            if (targetIsFixedSuperAdmin && req.status() == UserStatus.BANNED) {
                return ApiResponse.fail("固定超级管理员 admin 不能被封禁");
            }
            target.setStatus(req.status());
        }

        if (req.role() != null && !req.role().isBlank()) {
            if (isSelf) {
                return ApiResponse.fail("不能修改当前登录账号的角色");
            }

            if (!AdminRole.isSuperAdminLike(admin.getRole())) {
                return ApiResponse.fail("只有固定超级管理员可以分配角色");
            }

            String newRole = AdminRole.normalize(req.role());
            if (!AdminRole.isAssignable(newRole)) {
                return ApiResponse.fail("不支持的角色：" + req.role());
            }

            if (AdminRole.SUPER_ADMIN.equals(newRole) && !targetIsFixedSuperAdmin) {
                return ApiResponse.fail("超级管理员只能是固定账号 admin，不能分配给其他账号");
            }

            if (targetIsFixedSuperAdmin && !AdminRole.SUPER_ADMIN.equals(newRole)) {
                return ApiResponse.fail("固定超级管理员 admin 的角色不能被改为其他角色");
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