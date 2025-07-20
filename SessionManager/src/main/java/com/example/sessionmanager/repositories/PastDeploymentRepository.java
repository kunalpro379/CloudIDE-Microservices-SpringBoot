package com.example.sessionmanager.repositories;

import com.example.sessionmanager.entities.PastDeployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PastDeploymentRepository extends JpaRepository<PastDeployment, UUID> {

     List<PastDeployment> findBySessionIdOrderByCreatedAtDesc(UUID sessionId);

     @Query("SELECT pd FROM PastDeployment pd WHERE pd.ownerId = :ownerId ORDER BY pd.createdAt DESC")
     List<PastDeployment> findByOwnerIdOrderByCreatedAtDesc(@Param("ownerId") UUID ownerId);

     @Query("SELECT pd FROM PastDeployment pd WHERE pd.sessionId = :sessionId AND pd.totalRuntimeMinutes IS NOT NULL ORDER BY pd.totalRuntimeMinutes DESC")
     List<PastDeployment> findMostUsedDeploymentsBySessionId(@Param("sessionId") UUID sessionId);
}