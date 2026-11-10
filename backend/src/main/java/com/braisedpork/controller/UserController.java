package com.braisedpork.controller;

import com.braisedpork.dto.Result;
import com.braisedpork.dto.UserAddressRequest;
import com.braisedpork.entity.User;
import com.braisedpork.security.JwtUtils;
import com.braisedpork.exception.UnauthorizedException;
import com.braisedpork.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<Result<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        Long userId = extractUserId(request);
        User user = userService.getUserInfo(userId);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole().name());
        userInfo.put("createTime", user.getCreateTime());

        return ResponseEntity.ok(Result.success(userInfo));
    }

    @PutMapping("/me")
    public ResponseEntity<Result<Map<String, Object>>> updateCurrentUser(
            HttpServletRequest request,
            @RequestBody Map<String, String> updateData) {
        Long userId = extractUserId(request);

        User updateInfo = new User();
        updateInfo.setUsername(updateData.get("username"));
        updateInfo.setEmail(updateData.get("email"));
        updateInfo.setPhone(updateData.get("phone"));
        updateInfo.setAvatar(updateData.get("avatar"));

        User updated = userService.updateUser(userId, updateInfo);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", updated.getId());
        userInfo.put("username", updated.getUsername());
        userInfo.put("email", updated.getEmail());
        userInfo.put("phone", updated.getPhone());
        userInfo.put("avatar", updated.getAvatar());
        userInfo.put("role", updated.getRole().name());

        return ResponseEntity.ok(Result.success("更新成功", userInfo));
    }

    @GetMapping("/me/address")
    public ResponseEntity<Result<Map<String, Object>>> getAddress(HttpServletRequest request) {
        User user = userService.getUserInfo(extractUserId(request));
        return ResponseEntity.ok(Result.success(toAddressMap(user)));
    }

    @PutMapping("/me/address")
    public ResponseEntity<Result<Map<String, Object>>> updateAddress(
            HttpServletRequest request,
            @jakarta.validation.Valid @RequestBody UserAddressRequest address) {
        User updated = userService.updateAddress(extractUserId(request), address);
        return ResponseEntity.ok(Result.success("收货地址已保存", toAddressMap(updated)));
    }

    private Map<String, Object> toAddressMap(User user) {
        Map<String, Object> address = new HashMap<>();
        address.put("receiverName", user.getReceiverName());
        address.put("receiverPhone", user.getReceiverPhone());
        address.put("province", user.getProvince());
        address.put("city", user.getCity());
        address.put("district", user.getDistrict());
        address.put("street", user.getStreet());
        address.put("isDefault", true);
        return address;
    }

    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("登录状态无效或已过期");
        }
        String token = authHeader.substring(7);
        return jwtUtils.getUserIdFromToken(token);
    }
}
