package com.ptit.library.dto;

import com.ptit.library.model.Message;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatPayload {
    private Integer id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime createdAt;
    private Boolean read;
    private String clientMsgId;

    public static ChatPayload from(Message m, String clientMsgId) {
        ChatPayload p = new ChatPayload();
        p.setId(m.getId());
        p.setSender(m.getSenderId());
        p.setReceiver(m.getReceiverId());
        p.setContent(m.getContent());
        p.setCreatedAt(m.getCreatedAt());
        p.setRead(Boolean.TRUE.equals(m.getIsRead()));
        p.setClientMsgId(clientMsgId);
        return p;
    }
}