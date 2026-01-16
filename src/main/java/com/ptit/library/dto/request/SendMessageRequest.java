package com.ptit.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Người nhận không được để trống")
    private String receiverId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
