package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.SessionInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionInvitationRepository extends JpaRepository<SessionInvitation, UUID> {

     List<SessionInvitation> findBySessionSessionId(UUID sessionId);

     List<SessionInvitation> findByEmail(String email);

     @Query("SELECT si FROM SessionInvitation si WHERE si.email = :email AND si.status = 'PENDING'")
     List<SessionInvitation> findPendingInvitationsByEmail(@Param("email") String email);

     @Query("SELECT si FROM SessionInvitation si WHERE si.session.sessionId = :sessionId AND si.status = 'PENDING'")
     List<SessionInvitation> findPendingInvitationsBySessionId(@Param("sessionId") UUID sessionId);

     void deleteBySessionSessionIdAndEmail(UUID sessionId, String email);
}