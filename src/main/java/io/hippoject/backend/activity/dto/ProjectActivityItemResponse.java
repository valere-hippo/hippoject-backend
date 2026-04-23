package io.hippoject.backend.activity.dto;

import java.time.Instant;

public record ProjectActivityItemResponse(
        String type,
        String title,
        String detail,
        Instant occurredAt) {
}
