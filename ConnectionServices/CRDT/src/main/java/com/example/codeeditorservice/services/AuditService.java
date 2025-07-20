package com.example.codeeditorservice.services;

import com.example.codeeditorservice.entities.AuditLog;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AuditService {

     void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId);

     void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId,
               Map<String, Object> oldValue, Map<String, Object> newValue);

     void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId,
               Map<String, Object> oldValue, Map<String, Object> newValue, String ipAddress, String userAgent);

     List<AuditLog> getWorkspaceAuditLogs(UUID wsId);

     List<AuditLog> getUserAuditLogs(UUID userId);

     List<AuditLog> getAuditLogsByAction(String action);

     List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate);

     void cleanupOldLogs(int daysToKeep);
}