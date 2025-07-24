-- Chat Service Database Schema

-- Chat rooms table
CREATE TABLE IF NOT EXISTS chat_rooms (
    room_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL UNIQUE REFERENCES sessions(session_id) ON DELETE CASCADE,
    room_name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    max_participants INTEGER DEFAULT 50,
    current_participants INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    settings JSONB
);

-- Chat participants table
CREATE TABLE IF NOT EXISTS chat_participants (
    participant_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID NOT NULL REFERENCES chat_rooms(room_id) ON DELETE CASCADE,
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'PARTICIPANT' CHECK(role IN ('MODERATOR', 'PARTICIPANT', 'OBSERVER')),
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    is_typing BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen TIMESTAMP,
    last_message_read_id UUID,
    UNIQUE(session_id, user_id)
);

-- Chat messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES sessions(session_id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT' CHECK(message_type IN ('TEXT', 'CODE_SNIPPET', 'FILE_SHARE', 'SYSTEM_MESSAGE', 'EMOJI_REACTION')),
    reply_to_message_id UUID REFERENCES chat_messages(message_id) ON DELETE SET NULL,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    metadata JSONB
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_chat_rooms_session_id ON chat_rooms(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_is_active ON chat_rooms(is_active);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_last_activity ON chat_rooms(last_activity);

CREATE INDEX IF NOT EXISTS idx_chat_participants_session_id ON chat_participants(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_participants_user_id ON chat_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_participants_is_online ON chat_participants(is_online);
CREATE INDEX IF NOT EXISTS idx_chat_participants_room_id ON chat_participants(room_id);

CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_user_id ON chat_messages(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_is_deleted ON chat_messages(is_deleted);
CREATE INDEX IF NOT EXISTS idx_chat_messages_reply_to ON chat_messages(reply_to_message_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_message_type ON chat_messages(message_type);

-- Trigger to update updated_at timestamp for chat messages
CREATE OR REPLACE FUNCTION update_chat_message_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_chat_messages_updated_at 
    BEFORE UPDATE ON chat_messages 
    FOR EACH ROW EXECUTE FUNCTION update_chat_message_updated_at();

-- Trigger to update last_activity in chat_rooms when a message is sent
CREATE OR REPLACE FUNCTION update_room_last_activity()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE chat_rooms 
    SET last_activity = CURRENT_TIMESTAMP 
    WHERE session_id = NEW.session_id;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_room_activity_on_message 
    AFTER INSERT ON chat_messages 
    FOR EACH ROW EXECUTE FUNCTION update_room_last_activity();
