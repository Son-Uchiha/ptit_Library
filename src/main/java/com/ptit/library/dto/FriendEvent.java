// src/main/java/com/ptit/library/dto/FriendEvent.java
package com.ptit.library.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendEvent {
    private String type;
    private String from;
    private String to;
    private LocalDateTime at;
}