package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.WorkspaceInvitation;
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
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {

     List<WorkspaceInvitation> findByInvitedUserId(UUID invitedUserId);

     List<WorkspaceInvitation> findByInvitedEmail(String invitedEmail);

     List<WorkspaceInvitation> findByInvitedUserIdAndStatus(UUID invitedUserId,
               WorkspaceInvitation.InvitationStatus status);

     List<WorkspaceInvitation> findByInvitedEmailAndStatus(String invitedEmail,
               WorkspaceInvitation.InvitationStatus status);

     @Query("SELECT wi FROM WorkspaceInvitation wi WHERE wi.workspace.wsId = :wsId AND wi.status = :status")
     List<WorkspaceInvitation> findByWorkspaceAndStatus(@Param("wsId") UUID wsId,
               @Param("status") WorkspaceInvitation.InvitationStatus status);

     Optional<WorkspaceInvitation> findByInvitationIdAndInvitedUserId(UUID invitationId, UUID invitedUserId);

     Optional<WorkspaceInvitation> findByInvitationIdAndInvitedEmail(UUID invitationId, String invitedEmail);

     @Query("SELECT wi FROM WorkspaceInvitation wi WHERE wi.invitedUserId = :userId OR wi.invitedEmail = :email")
     List<WorkspaceInvitation> findByUserIdOrEmail(@Param("userId") UUID userId, @Param("email") String email);

     @Modifying
     @Transactional
     @Query("UPDATE WorkspaceInvitation wi SET wi.status = :status WHERE wi.invitationId = :invitationId")
     void updateInvitationStatus(@Param("invitationId") UUID invitationId,
               @Param("status") WorkspaceInvitation.InvitationStatus status);

     @Modifying
     @Transactional
     @Query("UPDATE WorkspaceInvitation wi SET wi.status = 'EXPIRED' WHERE wi.expiresAt < :currentTime AND wi.status = 'PENDING'")
     void expireOldInvitations(@Param("currentTime") LocalDateTime currentTime);

     boolean existsByWorkspaceWsIdAndInvitedUserId(UUID wsId, UUID invitedUserId);

     boolean existsByWorkspaceWsIdAndInvitedEmail(UUID wsId, String invitedEmail);
}