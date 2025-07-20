package com.example.codeeditorservice.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "workspaces")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workspace {

     @Id
     private String id; // MongoDB ObjectId

     @Field("ws_id")
     private UUID wsId;

     @Field("owner_id")
     private UUID ownerId;

     private String name;
     private String description;

     private WorkspaceSettings settings;
     private List<WorkspaceFile> files = new ArrayList<>();
     private WorkspaceEnvironment environment;
     private WorkspaceDeployment deployment;
     private WorkspaceGit git;
     private WorkspaceCollaboration collaboration;

     @Field("template_id")
     private UUID templateId;

     @Field("is_public")
     private Boolean isPublic = false;

     @Field("is_template")
     private Boolean isTemplate = false;

     private List<String> tags = new ArrayList<>();

     @Field("created_at")
     private LocalDateTime createdAt;

     @Field("updated_at")
     private LocalDateTime updatedAt;

     @Field("last_active")
     private LocalDateTime lastActive;

     private WorkspaceAnalytics analytics;

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceSettings {
          private String language;
          private String theme;
          @Field("font_size")
          private Integer fontSize;
          @Field("tab_size")
          private Integer tabSize;
          @Field("word_wrap")
          private Boolean wordWrap;
          @Field("auto_save")
          private Boolean autoSave;
          @Field("auto_save_interval")
          private Integer autoSaveInterval;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceFile {
          @Field("file_id")
          private UUID fileId;
          private String name;
          private String path;
          private String type; // 'file' or 'directory'
          private String content;
          private Long size;
          @Field("mime_type")
          private String mimeType;
          @Field("created_at")
          private LocalDateTime createdAt;
          @Field("updated_at")
          private LocalDateTime updatedAt;
          @Field("parent_id")
          private UUID parentId;
          private WorkspaceFilePermissions permissions;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceFilePermissions {
          private Boolean read;
          private Boolean write;
          private Boolean execute;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceEnvironment {
          private String runtime;
          private String version;
          private List<WorkspaceDependency> dependencies = new ArrayList<>();
          @Field("environment_variables")
          private Map<String, String> environmentVariables;
          @Field("startup_command")
          private String startupCommand;
          private Integer port;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceDependency {
          private String name;
          private String version;
          private String type; // 'npm', 'pip', 'maven', etc.
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceDeployment {
          private String status; // 'stopped', 'starting', 'running', 'error'
          @Field("container_id")
          private String containerId;
          @Field("image_name")
          private String imageName;
          private List<WorkspacePort> ports = new ArrayList<>();
          @Field("last_deployed")
          private LocalDateTime lastDeployed;
          @Field("deployment_logs")
          private List<WorkspaceDeploymentLog> deploymentLogs = new ArrayList<>();
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspacePort {
          private Integer internal;
          private Integer external;
          private String name;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceDeploymentLog {
          private LocalDateTime timestamp;
          private String level;
          private String message;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceGit {
          @Field("repository_url")
          private String repositoryUrl;
          private String branch;
          @Field("commit_hash")
          private String commitHash;
          @Field("last_sync")
          private LocalDateTime lastSync;
          @Field("sync_enabled")
          private Boolean syncEnabled;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceCollaboration {
          @Field("active_users")
          private List<WorkspaceActiveUser> activeUsers = new ArrayList<>();
          @Field("chat_history")
          private List<WorkspaceChatMessage> chatHistory = new ArrayList<>();
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceActiveUser {
          @Field("user_id")
          private UUID userId;
          private String username;
          @Field("cursor_position")
          private WorkspaceCursorPosition cursorPosition;
          private WorkspaceSelection selection;
          @Field("last_activity")
          private LocalDateTime lastActivity;
          @Field("session_id")
          private String sessionId;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceCursorPosition {
          @Field("file_id")
          private UUID fileId;
          private Integer line;
          private Integer column;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceSelection {
          private WorkspacePosition start;
          private WorkspacePosition end;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspacePosition {
          private Integer line;
          private Integer column;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceChatMessage {
          @Field("message_id")
          private UUID messageId;
          @Field("user_id")
          private UUID userId;
          private String username;
          private String message;
          private LocalDateTime timestamp;
          @Field("reply_to")
          private UUID replyTo;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceAnalytics {
          @Field("total_files")
          private Integer totalFiles;
          @Field("total_size")
          private Long totalSize;
          @Field("language_stats")
          private Map<String, Integer> languageStats;
          @Field("activity_stats")
          private WorkspaceActivityStats activityStats;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class WorkspaceActivityStats {
          @Field("daily_active_users")
          private Integer dailyActiveUsers;
          @Field("total_edits")
          private Integer totalEdits;
          @Field("total_runs")
          private Integer totalRuns;
     }
}