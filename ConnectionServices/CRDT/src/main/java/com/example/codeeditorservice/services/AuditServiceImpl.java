package com.example.codeeditorservice.services;

import com.example.codeeditorservice.entities.AuditLog;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.repository.AuditLogRepository;
import com.example.codeeditorservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

     private final AuditLogRepository auditLogRepository;
     private final UserRepository userRepository;

     @Override
     @Async
     @Transactional
     public void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId) {
          logAction(wsId, userId, action, resourceType, resourceId, null, null, null, null);
     }

     @Override
     @Async
     @Transactional
     public void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId,
               Map<String, Object> oldValue, Map<String, Object> newValue) {
          logAction(wsId, userId, action, resourceType, resourceId, oldValue, newValue, null, null);
     }

     @Override
     @Async
     @Transactional
     public void logAction(UUID wsId, UUID userId, String action, String resourceType, String resourceId,
               Map<String, Object> oldValue, Map<String, Object> newValue, String ipAddress, String userAgent) {

          log.debug("Logging audit action: {} for user: {} on resource: {}", action, userId, resourceId);

          User user = null;
          if (userId != null) {
               user = userRepository.findById(userId).orElse(null);
          }

          AuditLog auditLog = AuditLog.builder()
                    .wsId(wsId)
                    .user(user)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

          auditLogRepository.save(auditLog);

          log.debug("Audit log created successfully with ID: {}", auditLog.getLogId());
     }

     @Override
     public List<AuditLog> getWorkspaceAuditLogs(UUID wsId) {
          return auditLogRepository.findByWsIdOrderByTimestampDesc(wsId);
     }

     @Override
     public List<AuditLog> getUserAuditLogs(UUID userId) {
          return auditLogRepository.findByUserUserIdOrderByTimestampDesc(userId);
     }

     @Override
     public List<AuditLog> getAuditLogsByAction(String action) {
          return auditLogRepository.findByActionOrderByTimestampDesc(action);
     }

     @Override
     public List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
          LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00", formatter);
          LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59", formatter);

          return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
     }

     @Override
     @Transactional
     public void cleanupOldLogs(int daysToKeep) {
          LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

          int deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffDate);

          log.info("Cleaned up {} audit logs older than {} days", deletedCount, daysToKeep);
     }
}