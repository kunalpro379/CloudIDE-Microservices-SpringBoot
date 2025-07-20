package com.example.sessionmanager.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInvitation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "invitation_id", updatable = false, nullable = false)
    private UUID invitationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionParticipant.ParticipantRole role = SessionParticipant.ParticipantRole.VIEWER;

    @Column(name = "message")
    private String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusDays(7); // 7 days expiry
    }

    public enum InvitationStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
} 