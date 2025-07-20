package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

     List<UserSession> findByUserId(UUID userId);

     List<UserSession> findByWorkspaceWsId(UUID wsId);

     List<UserSession> findByUserIdAndStatus(UUID userId, UserSession.SessionStatus status);

     List<UserSession> findByWorkspaceWsIdAndStatus(UUID wsId, UserSession.SessionStatus status);

     Optional<UserSession> findByUserIdAndWorkspaceWsIdAndStatus(UUID userId, UUID wsId,
               UserSession.SessionStatus status);

     @Query("SELECT us FROM UserSession us WHERE us.userId = :userId AND us.workspace.wsId = :wsId AND us.status = 'ACTIVE'")
     Optional<UserSession> findActiveSessionByUserAndWorkspace(@Param("userId") UUID userId, @Param("wsId") UUID wsId);

     @Query("SELECT us FROM UserSession us WHERE us.workspace.wsId = :wsId AND us.status = 'ACTIVE'")
     List<UserSession> findActiveSessionsByWorkspace(@Param("wsId") UUID wsId);

     @Modifying
     @Transactional
     @Query("UPDATE UserSession us SET us.status = 'INACTIVE', us.endedAt = :endTime WHERE us.userId = :userId AND us.workspace.wsId = :wsId AND us.status = 'ACTIVE'")
     void endUserSessionsInWorkspace(@Param("userId") UUID userId, @Param("wsId") UUID wsId,
               @Param("endTime") LocalDateTime endTime);

     @Modifying
     @Transactional
     @Query("UPDATE UserSession us SET us.status = 'INACTIVE', us.endedAt = :endTime WHERE us.userId = :userId AND us.status = 'ACTIVE'")
     void endAllUserSessions(@Param("userId") UUID userId, @Param("endTime") LocalDateTime endTime);

     @Modifying
     @Transactional
     @Query("UPDATE UserSession us SET us.status = 'DISCONNECTED' WHERE us.lastActivity < :cutoffTime AND us.status = 'ACTIVE'")
     void markInactiveSessionsAsDisconnected(@Param("cutoffTime") LocalDateTime cutoffTime);

     @Query("SELECT COUNT(us) FROM UserSession us WHERE us.workspace.wsId = :wsId AND us.status = 'ACTIVE'")
     long countActiveSessionsByWorkspace(@Param("wsId") UUID wsId);

     @Query("SELECT COUNT(us) FROM UserSession us WHERE us.userId = :userId AND us.status = 'ACTIVE'")
     long countActiveSessionsByUser(@Param("userId") UUID userId);
}