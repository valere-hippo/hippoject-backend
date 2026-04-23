CREATE TABLE sprints (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    goal VARCHAR(1000) NOT NULL,
    starts_at DATE NOT NULL,
    ends_at DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sprints_project_id_starts_at ON sprints (project_id, starts_at DESC);

ALTER TABLE issues
    ADD COLUMN sprint_id BIGINT REFERENCES sprints (id) ON DELETE SET NULL;

CREATE INDEX idx_issues_sprint_id ON issues (sprint_id);
