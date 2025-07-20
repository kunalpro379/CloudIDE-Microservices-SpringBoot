package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {

     Optional<Deployment> findBySessionId(UUID sessionId);

     @Query("SELECT d FROM Deployment d WHERE d.sessionId = :sessionId AND (d.status = 'RUNNING' OR d.status = 'STARTING')")
     Optional<Deployment> findActiveDeploymentBySessionId(@Param("sessionId") UUID sessionId);

     @Query("SELECT d FROM Deployment d WHERE d.ownerId = :ownerId")
     List<Deployment> findByOwnerId(@Param("ownerId") UUID ownerId);

     @Query("SELECT d FROM Deployment d WHERE d.status = 'RUNNING'")
     List<Deployment> findRunningDeployments();

     @Query("SELECT d FROM Deployment d WHERE d.status = 'RUNNING' AND d.sessionId IN " +
               "(SELECT dp.deployment.sessionId FROM DeploymentParticipant dp WHERE dp.userId = :userId AND dp.isActive = true)")
     List<Deployment> findRunningDeploymentsByParticipantId(@Param("userId") UUID userId);
}