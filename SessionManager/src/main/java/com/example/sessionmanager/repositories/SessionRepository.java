package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

     @Query("SELECT s FROM Session s WHERE s.ownerId = :ownerId AND s.status != 'ARCHIVED'")
     List<Session> findByOwnerId(@Param("ownerId") UUID ownerId);

     @Query("SELECT COUNT(s) FROM Session s WHERE s.ownerId = :ownerId AND s.status != 'ARCHIVED'")
     long countByOwnerId(@Param("ownerId") UUID ownerId);

     @Query("SELECT s FROM Session s WHERE s.sessionId IN " +
               "(SELECT sp.session.sessionId FROM SessionParticipant sp WHERE sp.userId = :userId AND sp.isActive = true)")
     List<Session> findSessionsByParticipantId(@Param("userId") UUID userId);

     @Query("SELECT s FROM Session s WHERE s.status = 'DEPLOYED' OR s.status = 'RUNNING'")
     List<Session> findActiveDeployments();

     Optional<Session> findBySessionIdAndStatus(UUID sessionId, Session.SessionStatus status);
}