UPDATE issues
SET
    issue_type = priority,
    priority = issue_type
WHERE issue_type IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')
  AND priority IN ('STORY', 'TASK', 'BUG', 'EPIC');
