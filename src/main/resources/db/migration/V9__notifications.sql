CREATE TABLE notifications (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_id VARCHAR(120) NOT NULL,
    type VARCHAR(40) NOT NULL,
    project_id BIGINT NOT NULL,
    issue_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    read BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
