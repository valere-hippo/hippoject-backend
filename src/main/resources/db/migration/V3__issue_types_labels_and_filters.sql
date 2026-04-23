ALTER TABLE issues
    ADD COLUMN issue_type VARCHAR(30) NOT NULL DEFAULT 'TASK';

CREATE TABLE issue_labels (
    issue_id BIGINT NOT NULL,
    label VARCHAR(50) NOT NULL,
    CONSTRAINT fk_issue_labels_issue FOREIGN KEY (issue_id) REFERENCES issues (id) ON DELETE CASCADE
);
