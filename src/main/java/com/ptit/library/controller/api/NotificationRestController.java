package com.ptit.library.controller.api;

import com.ptit.library.dto.request.CreateNotificationRequest;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.NotificationResponse;
import com.ptit.library.model.Notification;
import com.ptit.library.service.NotificationService;
import com.ptit.library.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;

    /**
     * GET /api/notifications
     * Lấy danh sách tất cả thông báo của user hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        List<Notification> notifications = notificationService.getNotificationsByUserId(username);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/notifications/unread
     * Lấy danh sách thông báo chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        List<Notification> notifications = notificationService.getUnreadNotifications(username);
        List<NotificationResponse> responses = notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/notifications/count
     * Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        long count = notificationService.countUnreadNotifications(username);
        Map<String, Long> data = new HashMap<>();
        data.put("unreadCount", count);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * POST /api/notifications
     * Tạo thông báo mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestBody CreateNotificationRequest request) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        String targetUserId = request.getTargetUserId();
        if (targetUserId == null || targetUserId.isBlank()) {
            targetUserId = username;
        }

        String notificationType = request.getNotificationType();
        if (notificationType == null || notificationType.isBlank()) {
            notificationType = "system";
        }

        Notification created = notificationService.createNotification(
                targetUserId,
                request.getContent(),
                notificationType);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toNotificationResponse(created), "Tạo thông báo thành công"));
    }

    /**
     * PUT /api/notifications/{id}/read
     * Đánh dấu thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Integer id) {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu là đọc"));
    }

    /**
     * PUT /api/notifications/read-all
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        notificationService.markAllAsRead(username);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả là đọc"));
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.getIsRead())
                .build();
    }
}
