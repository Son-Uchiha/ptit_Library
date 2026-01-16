package com.ptit.library.controller;

import com.ptit.library.dto.ChatPayload;
import com.ptit.library.model.Message;
import com.ptit.library.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * This controller now only handles WebSocket messaging.
 * For REST API endpoints, use:
 * - POST /api/messages - Send message
 * - GET /api/messages/conversation/{userId} - Get conversation
 * - GET /api/messages/unread - Get unread messages
 * - PUT /api/messages/{id}/read - Mark as read
 * 
 * @see com.ptit.library.controller.api.MessageRestController
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ====== STOMP: nhận từ FE, lưu DB, đẩy realtime ======
    @MessageMapping("/chat.private")
    public void handlePrivate(@Payload ChatPayload incoming, Principal principal) {
        String sender = (principal != null ? principal.getName() : incoming.getSender());
        String receiver = incoming.getReceiver();

        Message saved = messageService.sendMessage(sender, receiver, incoming.getContent());

        ChatPayload out = ChatPayload.from(saved, incoming.getClientMsgId());

        // Gửi đến cả 2 user qua topic (không cần authentication)
        messagingTemplate.convertAndSend("/topic/messages/" + receiver, out);
        messagingTemplate.convertAndSend("/topic/messages/" + sender, out);
    }
}
