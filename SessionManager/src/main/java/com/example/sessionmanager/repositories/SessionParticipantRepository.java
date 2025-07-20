package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, UUID> {

     List<SessionParticipant> findBySessionSessionId(UUID sessionId);

     Optional<SessionParticipant> findBySessionSessionIdAndUserId(UUID sessionId, UUID userId);

     Optional<SessionParticipant> findBySessionSessionIdAndEmail(UUID sessionId, String email);

     @Query("SELECT sp FROM SessionParticipant sp WHERE sp.userId = :userId AND sp.isActive = true")
     List<SessionParticipant> findActiveSessionsByUserId(@Param("userId") UUID userId);

     @Query("SELECT sp FROM SessionParticipant sp WHERE sp.session.sessionId = :sessionId AND sp.role = 'OWNER'")
     Optional<SessionParticipant> findOwnerBySessionId(@Param("sessionId") UUID sessionId);

     void deleteBySessionSessionIdAndUserId(UUID sessionId, UUID userId);
}