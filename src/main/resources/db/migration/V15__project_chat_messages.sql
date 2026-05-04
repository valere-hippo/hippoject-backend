CREATE TABLE project_chat_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    author_id VARCHAR(120) NOT NULL,
    author_display_name VARCHAR(120) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_project_chat_messages_project_created
    ON project_chat_messages(project_id, created_at);
