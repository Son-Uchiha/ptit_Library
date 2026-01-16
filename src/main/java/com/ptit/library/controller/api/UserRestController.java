package com.ptit.library.controller.api;

import com.ptit.library.dto.request.ChangePasswordRequest;
import com.ptit.library.dto.request.UpdateProfileRequest;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.UserResponse;
import com.ptit.library.model.User;
import com.ptit.library.service.UserService;
import com.ptit.library.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users/me
     * Lấy thông tin user hiện tại
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        Optional<User> userOpt = userService.findUserWithStudentInfo(username);
        if (userOpt.isEmpty()) {
            userOpt = userService.findByUsername(username);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng", 404));
        }

        User user = userOpt.get();
        UserResponse response = toUserResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/users/{username}
     * Lấy thông tin user theo username
     */
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        Optional<User> userOpt = userService.findUserWithStudentInfo(username);
        if (userOpt.isEmpty()) {
            userOpt = userService.findByUsername(username);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy người dùng", 404));
        }

        User user = userOpt.get();
        UserResponse response = toUserResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PUT /api/users/me
     * Cập nhật thông tin cá nhân
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            LocalDate dob = null;
            if (request.getDateOfBirth() != null && !request.getDateOfBirth().trim().isEmpty()) {
                dob = LocalDate.parse(request.getDateOfBirth());
            }

            boolean updated = userService.updatePersonalInfo(
                    username,
                    request.getFullName(),
                    dob,
                    request.getGender(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getMajor());

            if (updated) {
                Optional<User> userOpt = userService.findUserWithStudentInfo(username);
                if (userOpt.isEmpty()) {
                    userOpt = userService.findByUsername(username);
                }

                UserResponse response = userOpt.map(this::toUserResponse).orElse(null);
                return ResponseEntity.ok(
                        ApiResponse.success(response, "Cập nhật thông tin cá nhân thành công"));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cập nhật thông tin cá nhân thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Định dạng dữ liệu không hợp lệ: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/users/me/password
     * Đổi mật khẩu
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getRetypePassword())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu nhập lại không khớp"));
        }

        boolean changed = userService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        if (changed) {
            return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mật khẩu cũ không chính xác"));
        }
    }

    /**
     * POST /api/users/me/avatar
     * Upload avatar
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
        }

        try {
            String uploadDir = "src/main/resources/static/images/avatars/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = username + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath);

            String avatarPath = "/images/avatars/" + fileName;
            userService.updateAvatar(username, avatarPath);

            return ResponseEntity.ok(ApiResponse.success(avatarPath, "Upload avatar thành công"));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload avatar: " + e.getMessage()));
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .phone(user.getPhone())
                .address(user.getAddress())
                .major(user.getMajor())
                .build();
    }
}
