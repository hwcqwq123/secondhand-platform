package com.example.secondhand.user;

import com.example.secondhand.config.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ApiResponse.ok(new UserProfileResponse(user));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        User user = getCurrentUser(authentication);

        user.setNickname(trimToNull(request.getNickname()));
        user.setAvatarUrl(trimToNull(request.getAvatarUrl()));
        user.setBio(trimToNull(request.getBio()));

        User saved = userRepository.save(user);
        return ApiResponse.ok(new UserProfileResponse(saved), "个人资料已保存");
    }

    @PutMapping("/me/payment")
    public ApiResponse<UserProfileResponse> updatePayment(
            Authentication authentication,
            @Valid @RequestBody UpdatePaymentRequest request
    ) {
        User user = getCurrentUser(authentication);

        user.setWechatQrUrl(trimToNull(request.getWechatQrUrl()));

        User saved = userRepository.save(user);
        return ApiResponse.ok(new UserProfileResponse(saved), "收款码已保存");
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "当前用户不存在"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}