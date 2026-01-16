package com.ptit.library.controller.api;

import com.ptit.library.dto.request.LoginRequest;
import com.ptit.library.dto.request.RegisterRequest;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.AuthResponse;
import com.ptit.library.dto.response.UserResponse;
import com.ptit.library.model.User;
import com.ptit.library.model.ValidationResponse;
import com.ptit.library.security.JwtTokenProvider;
import com.ptit.library.service.FriendService;
import com.ptit.library.service.PasswordResetService;
import com.ptit.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * POST /api/auth/login
     * Đăng nhập và nhận JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            Optional<User> userOpt = userService.findUserWithStudentInfo(request.getUsername());
            if (userOpt.isEmpty()) {
                userOpt = userService.findByUsername(request.getUsername());
            }

            UserResponse userResponse = null;
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userResponse = UserResponse.builder()
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

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationInSeconds())
                    .user(userResponse)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(authResponse, "Đăng nhập thành công"));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Tên đăng nhập hoặc mật khẩu không chính xác", 401));
        }
    }

    /**
     * POST /api/auth/register
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Validate passwords match
            if (!request.getPassword().equals(request.getRetypePassword())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Mật khẩu nhập lại không khớp"));
            }

            // Check if account exists
            if (userService.checkAccountExists(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Tài khoản đã tồn tại"));
            }

            // Check if email exists
            if (userService.checkEmailExists(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email đã được sử dụng"));
            }

            // Validate user data
            ValidationResponse validation = userService.validateUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRetypePassword());

            if (!validation.isValid()) {
                String errorMessage = validation.getGlobalMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    if (validation.getUsernameError() != null) {
                        errorMessage = validation.getUsernameError();
                    } else if (validation.getEmailError() != null) {
                        errorMessage = validation.getEmailError();
                    } else if (validation.getPasswordError() != null) {
                        errorMessage = validation.getPasswordError();
                    }
                }
                return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
            }

            // Create user
            User user = new User(request.getUsername(), request.getPassword(), request.getEmail(), "USER");

            if (!userService.registerUser(user)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Đăng ký thất bại"));
            }

            // Save additional student info if provided
            try {
                LocalDate dob = null;
                if (request.getDateOfBirth() != null && !request.getDateOfBirth().trim().isEmpty()) {
                    dob = LocalDate.parse(request.getDateOfBirth());
                }
                userService.updatePersonalInfo(
                        request.getUsername(),
                        request.getFullName(),
                        dob,
                        request.getGender(),
                        request.getPhone(),
                        request.getAddress(),
                        request.getMajor());
            } catch (Exception e) {
                System.err.println("Error saving student info: " + e.getMessage());
                e.printStackTrace();
                // Continue even if student info save fails
            }

            // Tự động kết bạn với admin
            try {
                friendService.autoAddAdminAsFriend(user.getUsername());
            } catch (Exception e) {
                System.err.println("Error adding admin as friend: " + e.getMessage());
                // Ignore if admin doesn't exist or already friends
            }

            UserResponse userResponse = UserResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .fullName(request.getFullName())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(userResponse, "Đăng ký thành công"));
        } catch (Exception e) {
            System.err.println("Register error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Yêu cầu reset mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng nhập tên đăng nhập"));
        }

        boolean success = passwordResetService.processForgotPassword(username);

        if (success) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Mật khẩu tạm thời sẽ được gửi tới email của bạn trong 1 phút. Vui lòng kiểm tra email."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không tìm thấy tài khoản hoặc gửi email thất bại. Vui lòng thử lại."));
        }
    }

    /**
     * POST /api/auth/refresh-token
     * Làm mới JWT token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token không hợp lệ", 401));
        }

        String newToken = tokenProvider.generateToken(authentication.getName());

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInSeconds())
                .build();

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token đã được làm mới"));
    }
}
