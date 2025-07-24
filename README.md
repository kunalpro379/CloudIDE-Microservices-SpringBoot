erDiagram
    users {
        UUID user_id PK
        VARCHAR(255) email UK
        VARCHAR(255) password_hash
        VARCHAR(100) username UK
        VARCHAR(100) first_name
        VARCHAR(100) last_name
        VARCHAR(500) profile_image_url
        BOOLEAN email_verified
        BOOLEAN is_active
        VARCHAR(50) oauth_provider
        VARCHAR(255) oauth_provider_id
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP last_login
    }

    workspaces {
        UUID ws_id PK
        VARCHAR(255) name
        TEXT description
        UUID owner_id FK
        BOOLEAN is_public
        BOOLEAN is_template
        VARCHAR(20) status
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP last_active
    }

    workspace_permissions {
        UUID ws_id PK,FK
        UUID user_id PK,FK
        VARCHAR(20) role
        UUID granted_by FK
        VARCHAR(20) status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    workspace_invitations {
        UUID invitation_id PK
        UUID ws_id FK
        UUID invited_by FK
        UUID invited_user_id FK
        VARCHAR(255) invited_email
        VARCHAR(20) role
        VARCHAR(20) status
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    sessions {
        UUID session_id PK
        VARCHAR(255) name
        TEXT description
        VARCHAR(100) language
        VARCHAR(100) framework
        BOOLEAN is_dynamic
        UUID owner_id FK
        VARCHAR(20) status
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP last_active
    }

    session_participants {
        UUID participant_id PK
        UUID session_id FK
        UUID user_id FK
        VARCHAR(255) email
        VARCHAR(20) role
        TIMESTAMP joined_at
        TIMESTAMP last_active
        BOOLEAN is_active
    }

    session_invitations {
        UUID invitation_id PK
        UUID session_id FK
        VARCHAR(255) email
        UUID invited_by FK
        VARCHAR(20) role
        TEXT message
        VARCHAR(20) status
        TIMESTAMP created_at
        TIMESTAMP expires_at
        TIMESTAMP responded_at
    }

    deployments {
        UUID deployment_id PK
        UUID session_id FK
        VARCHAR(500) deployment_url
        VARCHAR(255) container_id
        INTEGER port
        VARCHAR(20) status
        UUID owner_id FK
        TIMESTAMP created_at
        TIMESTAMP started_at
        TIMESTAMP stopped_at
        TIMESTAMP last_active
    }

    deployment_participants {
        UUID participant_id PK
        UUID deployment_id FK
        UUID user_id FK
        VARCHAR(255) email
        VARCHAR(20) role
        TIMESTAMP joined_at
        TIMESTAMP last_active
        BOOLEAN is_active
    }

    past_deployments {
        UUID past_deployment_id PK
        UUID original_deployment_id FK
        UUID session_id FK
        VARCHAR(500) deployment_url
        VARCHAR(255) container_id
        INTEGER port
        UUID owner_id FK
        TIMESTAMP created_at
        TIMESTAMP started_at
        TIMESTAMP stopped_at
        BIGINT total_runtime_minutes
        VARCHAR(255) reason_for_stop
    }

    audit_logs {
        SERIAL log_id PK
        UUID ws_id FK
        UUID user_id FK
        VARCHAR(100) action
        VARCHAR(50) resource_type
        VARCHAR(255) resource_id
        JSONB old_value
        JSONB new_value
        INET ip_address
        TEXT user_agent
        TIMESTAMP timestamp
    }

    refresh_tokens {
        UUID token_id PK
        UUID user_id FK
        VARCHAR(255) token_hash
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP revoked_at
        VARCHAR(500) device_info
    }

    user_sessions {
        UUID session_id PK
        UUID user_id FK
        UUID ws_id FK
        VARCHAR(20) status
        TIMESTAMP last_activity
        TIMESTAMP created_at
        TIMESTAMP ended_at
    }

    mongo_workspaces {
        ObjectId _id PK
        UUID ws_id FK
        UUID owner_id FK
        String name
        Object settings
        Array files
        Object environment
        Object deployment
        Object git
        Object collaboration
        Array tags
        Object analytics
    }

    users ||--o{ workspaces : "owns"
    users ||--o{ workspace_permissions : "has access to"
    users ||--o{ workspace_invitations : "invites/is invited"
    users ||--o{ refresh_tokens : "has"
    users ||--o{ user_sessions : "has"
    users ||--o{ audit_logs : "generates"
    
    workspaces ||--o{ workspace_permissions : "has"
    workspaces ||--o{ workspace_invitations : "has"
    workspaces ||--o{ user_sessions : "has"
    workspaces ||--o{ audit_logs : "has"
    
    sessions ||--o{ session_participants : "has"
    sessions ||--o{ session_invitations : "has"
    sessions ||--o{ deployments : "has"
    sessions ||--o{ past_deployments : "has"
    
    deployments ||--o{ deployment_participants : "has"
    deployments ||--o{ past_deployments : "tracks"

    workspaces ||--|| mongo_workspaces : "extends to NoSQL"
