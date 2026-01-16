package com.ptit.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Integer id;
    private String senderId;
    private String receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
