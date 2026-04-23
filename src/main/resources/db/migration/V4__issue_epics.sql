ALTER TABLE issues
    ADD COLUMN epic_issue_id BIGINT NULL,
    ADD CONSTRAINT fk_issues_epic_issue FOREIGN KEY (epic_issue_id) REFERENCES issues (id) ON DELETE SET NULL;
