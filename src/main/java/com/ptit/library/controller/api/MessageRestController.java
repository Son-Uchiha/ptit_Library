package com.ptit.library.controller.api;

import com.ptit.library.dto.ChatPayload;
import com.ptit.library.dto.request.SendMessageRequest;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.MessageResponse;
import com.ptit.library.model.Message;
import com.ptit.library.service.MessageService;
import com.ptit.library.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * POST /api/messages
     * Gửi tin nhắn mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {

        String senderId = SecurityUtil.getAuthenticatedUsername();
        if (senderId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        Message saved = messageService.sendMessage(senderId, request.getReceiverId(), request.getContent());

        // Send realtime notification via WebSocket using topic (broadcast)
        ChatPayload payload = ChatPayload.from(saved, null);
        // Gửi tới topic của receiver và sender
        messagingTemplate.convertAndSend("/topic/messages/" + request.getReceiverId(), payload);
        messagingTemplate.convertAndSend("/topic/messages/" + senderId, payload);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toMessageResponse(saved), "Gửi tin nhắn thành công"));
    }

    /**
     * GET /api/messages/conversation/{userId}
     * Lấy cuộc hội thoại với một user
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(
            @PathVariable String userId) {

        String currentUser = SecurityUtil.getAuthenticatedUsername();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        List<Message> messages = messageService.getConversation(currentUser, userId);
        List<MessageResponse> responses = messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/messages/unread
     * Lấy danh sách tin nhắn chưa đọc
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getUnreadMessages() {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        List<Message> messages = messageService.getUnreadMessages(username);
        List<MessageResponse> responses = messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * PUT /api/messages/{id}/read
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Integer id) {
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        messageService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tin nhắn là đã đọc"));
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
