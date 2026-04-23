ALTER TABLE saved_filters
    ADD COLUMN priority VARCHAR(30) NULL,
    ADD COLUMN assignee_id VARCHAR(120) NULL;
