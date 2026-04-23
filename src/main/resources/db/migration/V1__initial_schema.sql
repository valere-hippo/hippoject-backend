CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    project_key VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    owner_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issues (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    issue_key VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(140) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    assignee_id VARCHAR(120),
    reporter_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_issues_project_id ON issues (project_id);
CREATE INDEX idx_issues_project_id_created_at ON issues (project_id, created_at DESC);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues (id) ON DELETE CASCADE,
    body VARCHAR(2000) NOT NULL,
    author_id VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comments_issue_id_created_at ON comments (issue_id, created_at ASC);
