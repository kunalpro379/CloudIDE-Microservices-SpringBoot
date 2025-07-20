package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.WorkspacePermission;
import com.example.sessionmanager.entities.WorkspacePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspacePermissionRepository extends JpaRepository<WorkspacePermission, WorkspacePermissionId> {

    List<WorkspacePermission> findByUserId(UUID userId);

    List<WorkspacePermission> findByWsId(UUID wsId);

    List<WorkspacePermission> findByUserIdAndStatus(UUID userId, WorkspacePermission.PermissionStatus status);

    List<WorkspacePermission> findByWsIdAndStatus(UUID wsId, WorkspacePermission.PermissionStatus status);

    Optional<WorkspacePermission> findByWsIdAndUserId(UUID wsId, UUID userId);

    @Query("SELECT wp FROM WorkspacePermission wp WHERE wp.wsId = :wsId AND wp.userId = :userId AND wp.status = :status")
    Optional<WorkspacePermission> findByWsIdAndUserIdAndStatus(@Param("wsId") UUID wsId, @Param("userId") UUID userId, @Param("status") WorkspacePermission.PermissionStatus status);

    @Query("SELECT wp FROM WorkspacePermission wp WHERE wp.wsId = :wsId AND wp.role = :role AND wp.status = 'ACCEPTED'")
    List<WorkspacePermission> findByWsIdAndRole(@Param("wsId") UUID wsId, @Param("role") WorkspacePermission.WorkspaceRole role);

    @Modifying
    @Transactional
    @Query("UPDATE WorkspacePermission wp SET wp.status = :status WHERE wp.wsId = :wsId AND wp.userId = :userId")
    void updatePermissionStatus(@Param("wsId") UUID wsId, @Param("userId") UUID userId, @Param("status") WorkspacePermission.PermissionStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM WorkspacePermission wp WHERE wp.wsId = :wsId AND wp.userId = :userId")
    void deletePermission(@Param("wsId") UUID wsId, @Param("userId") UUID userId);

    boolean existsByWsIdAndUserId(UUID wsId, UUID userId);
} 