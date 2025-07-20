-- Session Management Database Schema

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    language VARCHAR(100) NOT NULL,
    framework VARCHAR(100),
    is_dynamic BOOLEAN DEFAULT FALSE,
    owner_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Session participants table
CREATE TABLE IF NOT EXISTS session_participants (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'VIEWER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Session invitations table
CREATE TABLE IF NOT EXISTS session_invitations (
    invitation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    invited_by UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'VIEWER',
    message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days'),
    responded_at TIMESTAMP
);

-- Deployments table
CREATE TABLE IF NOT EXISTS deployments (
    deployment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    deployment_url VARCHAR(500),
    container_id VARCHAR(255),
    port INTEGER,
    status VARCHAR(20) DEFAULT 'CREATED',
    owner_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deployment participants table
CREATE TABLE IF NOT EXISTS deployment_participants (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deployment_id UUID NOT NULL REFERENCES deployments(deployment_id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'VIEWER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Past deployments table
CREATE TABLE IF NOT EXISTS past_deployments (
    past_deployment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_deployment_id UUID NOT NULL,
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    deployment_url VARCHAR(500),
    container_id VARCHAR(255),
    port INTEGER,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    total_runtime_minutes BIGINT,
    reason_for_stop VARCHAR(255)
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_sessions_owner_id ON sessions(owner_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON sessions(status);
CREATE INDEX IF NOT EXISTS idx_session_participants_session_id ON session_participants(session_id);
CREATE INDEX IF NOT EXISTS idx_session_participants_user_id ON session_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_session_participants_email ON session_participants(email);
CREATE INDEX IF NOT EXISTS idx_session_invitations_session_id ON session_invitations(session_id);
CREATE INDEX IF NOT EXISTS idx_session_invitations_email ON session_invitations(email);
CREATE INDEX IF NOT EXISTS idx_session_invitations_status ON session_invitations(status);
CREATE INDEX IF NOT EXISTS idx_deployments_session_id ON deployments(session_id);
CREATE INDEX IF NOT EXISTS idx_deployments_owner_id ON deployments(owner_id);
CREATE INDEX IF NOT EXISTS idx_deployments_status ON deployments(status);
CREATE INDEX IF NOT EXISTS idx_deployment_participants_deployment_id ON deployment_participants(deployment_id);
CREATE INDEX IF NOT EXISTS idx_deployment_participants_user_id ON deployment_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_past_deployments_session_id ON past_deployments(session_id);
CREATE INDEX IF NOT EXISTS idx_past_deployments_owner_id ON past_deployments(owner_id);

-- Constraints
ALTER TABLE session_participants ADD CONSTRAINT chk_session_participant_role 
    CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER'));

ALTER TABLE session_invitations ADD CONSTRAINT chk_session_invitation_role 
    CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER'));

ALTER TABLE session_invitations ADD CONSTRAINT chk_session_invitation_status 
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED'));

ALTER TABLE deployments ADD CONSTRAINT chk_deployment_status 
    CHECK (status IN ('CREATED', 'STARTING', 'RUNNING', 'STOPPING', 'STOPPED', 'FAILED'));

ALTER TABLE deployment_participants ADD CONSTRAINT chk_deployment_participant_role 
    CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER'));

ALTER TABLE sessions ADD CONSTRAINT chk_session_status 
    CHECK (status IN ('CREATED', 'DEPLOYED', 'RUNNING', 'STOPPED', 'ARCHIVED')); 