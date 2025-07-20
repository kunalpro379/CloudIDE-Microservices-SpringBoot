package com.example.codeeditorservice.repository;

import com.example.codeeditorservice.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

     List<AuditLog> findByWsIdOrderByTimestampDesc(UUID wsId);

     List<AuditLog> findByUserUserIdOrderByTimestampDesc(UUID userId);

     List<AuditLog> findByActionOrderByTimestampDesc(String action);

     List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

     List<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType);

     List<AuditLog> findByWsIdAndActionOrderByTimestampDesc(UUID wsId, String action);

     @Modifying
     @Transactional
     @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
     int deleteByTimestampBefore(LocalDateTime cutoffDate);

     @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.wsId = :wsId AND a.timestamp > :since")
     long countWorkspaceActivitiesSince(UUID wsId, LocalDateTime since);

     @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.user.userId = :userId AND a.timestamp > :since")
     long countUserActivitiesSince(UUID userId, LocalDateTime since);
}