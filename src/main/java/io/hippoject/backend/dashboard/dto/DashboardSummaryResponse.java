package io.hippoject.backend.dashboard.dto;

public record DashboardSummaryResponse(
        long projects,
        long openIssues,
        long inFlightIssues,
        long criticalIssues,
        long activeSprints,
        long epics) {
}
