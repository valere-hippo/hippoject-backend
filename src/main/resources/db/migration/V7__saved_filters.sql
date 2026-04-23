CREATE TABLE saved_filters (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id VARCHAR(120) NOT NULL,
    name VARCHAR(120) NOT NULL,
    query VARCHAR(200),
    project_id BIGINT NULL,
    status VARCHAR(30),
    issue_type VARCHAR(30),
    label VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);
