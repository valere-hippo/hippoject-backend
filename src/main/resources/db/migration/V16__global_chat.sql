CREATE TABLE chat_conversations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(160),
    group_chat BOOLEAN NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_participants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    user_id VARCHAR(120) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    avatar_url TEXT,
    joined_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_chat_participant UNIQUE (conversation_id, user_id)
);

CREATE TABLE chat_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    author_id VARCHAR(120) NOT NULL,
    author_display_name VARCHAR(120) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_chat_participants_user ON chat_participants(user_id);
CREATE INDEX idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at);
