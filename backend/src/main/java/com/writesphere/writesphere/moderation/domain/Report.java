package com.writesphere.writesphere.moderation.domain;

import java.time.Instant;
import java.util.UUID;

public record Report(UUID id, UUID workId, UUID reporterId, String reason, Instant createdAt,
                     Decision decision, Instant resolvedAt, UUID moderatorId) {
    public enum Decision { DISMISS, HIDE }

    public static Report open(UUID workId, UUID reporterId, String reason, Instant now) {
        return new Report(UUID.randomUUID(), workId, reporterId, reason.strip(), now, null, null, null);
    }

    public boolean isResolved() {
        return decision != null;
    }

    public Report resolve(Decision decision, UUID moderatorId, Instant now) {
        return new Report(id, workId, reporterId, reason, createdAt, decision, now, moderatorId);
    }
}
