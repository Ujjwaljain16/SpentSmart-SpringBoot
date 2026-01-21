package com.expenseTracker.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys",
    indexes = {
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey extends BaseEntity {

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String key;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
