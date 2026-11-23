package com.writesphere.writesphere.moderation.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.moderation.domain.Report;
import org.springframework.stereotype.Service;

@Service
public class ModerationQueries {
    private final ReadModel reports;

    public ModerationQueries(ReadModel reports) {
        this.reports = reports;
    }

    public record Summary(UUID id, UUID workId, String title, String author, String reason, Instant createdAt,
                          Report.Decision decision, Instant resolvedAt) {}

    public interface ReadModel {
        List<Summary> list(boolean resolved);
    }

    public List<Summary> list(boolean resolved) {
        return reports.list(resolved);
    }
}
