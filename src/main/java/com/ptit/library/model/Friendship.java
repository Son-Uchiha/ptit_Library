package com.ptit.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // hai đầu quan hệ
    @Column(name = "user_id", length = 20, nullable = false)
    private String userId;

    @Column(name = "friend_id", length = 20, nullable = false)
    private String friendId;

    // pending | accepted | blocked
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    // ai gửi lời mời
    @Column(name = "requested_by", length = 20)
    private String requestedBy;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "responded_at")
    private Timestamp respondedAt;

    @Column(name = "last_interaction_at")
    private Timestamp lastInteractionAt;

    // Cột để đảm bảo unique constraint (u_min, u_max)
    @Column(name = "u_min")
    private String uMin;

    @Column(name = "u_max")
    private String uMax;

    @PrePersist
    @PreUpdate
    private void setOrderedPair() {
        // Đảm bảo u_min < u_max theo thứ tự alphabet để tránh duplicate
        if (userId != null && friendId != null) {
            if (userId.compareTo(friendId) <= 0) {
                this.uMin = userId;
                this.uMax = friendId;
            } else {
                this.uMin = friendId;
                this.uMax = userId;
            }
        }
    }
}