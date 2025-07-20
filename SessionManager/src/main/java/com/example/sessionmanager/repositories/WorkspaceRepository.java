package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

     List<Workspace> findByOwnerId(UUID ownerId);

     List<Workspace> findByOwnerIdAndStatus(UUID ownerId, Workspace.WorkspaceStatus status);

     Optional<Workspace> findByWsIdAndStatus(UUID wsId, Workspace.WorkspaceStatus status);

     @Query("SELECT w FROM Workspace w WHERE w.isPublic = true AND w.status = :status")
     List<Workspace> findPublicWorkspaces(@Param("status") Workspace.WorkspaceStatus status);

     @Query("SELECT w FROM Workspace w JOIN w.permissions p WHERE p.userId = :userId AND p.status = 'ACCEPTED' AND w.status = :status")
     List<Workspace> findWorkspacesByUserAccess(@Param("userId") UUID userId,
               @Param("status") Workspace.WorkspaceStatus status);

     @Query("SELECT w FROM Workspace w WHERE w.name LIKE %:name% AND w.status = :status")
     List<Workspace> findByNameContainingAndStatus(@Param("name") String name,
               @Param("status") Workspace.WorkspaceStatus status);

     boolean existsByNameAndOwnerId(String name, UUID ownerId);
}