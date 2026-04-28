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
     *
     * 新增 SUPER_ADMIN 唯一规则：
     * 1. SUPER_ADMIN 只能由系统初始化创建；
     * 2. 任何管理员都不能把别人设置成 SUPER_ADMIN；
     * 3. SUPER_ADMIN 账号不能被别人修改角色；
     * 4. SUPER_ADMIN 账号不能被封禁；
     * 5. 管理员不能修改自己的角色，也不能封禁自己。
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
        boolean targetIsSuperAdmin = AdminRole.SUPER_ADMIN.equals(target.getRole());

        String oldRole = target.getRole();
        UserStatus oldStatus = target.getStatus();

        /**
         * 规则一：
         * SUPER_ADMIN 是系统唯一最高管理员，不能被封禁。
         */
        if (targetIsSuperAdmin && req.status() != null && req.status() != target.getStatus()) {
            return ApiResponse.fail("SUPER_ADMIN 是系统唯一超级管理员，不能修改其账号状态");
        }

        /**
         * 规则二：
         * 管理员不能封禁自己。
         */
        if (req.status() != null) {
            if (isSelf && req.status() == UserStatus.BANNED) {
                return ApiResponse.fail("不能封禁当前登录的管理员账号");
            }

            target.setStatus(req.status());
        }

        /**
         * 规则三：
         * 处理角色修改。
         */
        if (req.role() != null && !req.role().isBlank()) {
            String newRole = AdminRole.normalize(req.role());

            /**
             * 管理员不能修改自己的角色，避免自己把自己改没权限。
             */
            if (isSelf) {
                return ApiResponse.fail("不能修改当前登录账号的角色");
            }

            /**
             * SUPER_ADMIN 账号唯一，不能被别人修改为其他角色。
             */
            if (targetIsSuperAdmin) {
                return ApiResponse.fail("SUPER_ADMIN 是系统唯一超级管理员，不能修改其角色");
            }

            /**
             * 任何管理员，包括 SUPER_ADMIN，都不能把别人设置成 SUPER_ADMIN。
             */
            if (AdminRole.SUPER_ADMIN.equals(newRole)) {
                return ApiResponse.fail("SUPER_ADMIN 账号唯一，不能通过后台手动分配 SUPER_ADMIN 权限");
            }

            /**
             * 只有 ADMIN / SUPER_ADMIN 这种高级管理员可以分配角色。
             */
            if (!AdminRole.isSuperAdminLike(admin.getRole())) {
                return ApiResponse.fail("只有超级管理员或兼容管理员可以分配角色");
            }

            /**
             * 后台可分配角色不包含 SUPER_ADMIN。
             */
            if (!AdminRole.isAssignableByAdmin(newRole)) {
                return ApiResponse.fail("不支持分配该角色：" + req.role());
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