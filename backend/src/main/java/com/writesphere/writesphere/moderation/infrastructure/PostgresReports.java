package com.writesphere.writesphere.moderation.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.moderation.application.ModerationCommands;
import com.writesphere.writesphere.moderation.application.ModerationQueries;
import com.writesphere.writesphere.moderation.domain.Report;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresReports implements ModerationCommands.Reports, ModerationQueries.ReadModel {
    private final JdbcClient jdbc;

    public PostgresReports(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean insertForVisibleWork(Report report) {
        return jdbc.sql("""
                WITH visible AS (SELECT id FROM published_works WHERE id = :work),
                filed AS (
                    INSERT INTO reports (id, work_id, reporter_id, reason, created_at)
                    SELECT :id, id, :reporter, :reason, :created FROM visible
                    ON CONFLICT (work_id, reporter_id) WHERE decision IS NULL DO NOTHING
                )
                SELECT EXISTS (SELECT 1 FROM visible)
                """).param("work", report.workId()).param("id", report.id()).param("reporter", report.reporterId())
                .param("reason", report.reason()).param("created", report.createdAt().atOffset(ZoneOffset.UTC))
                .query(Boolean.class).single();
    }

    @Override
    public Optional<Report> lock(UUID id) {
        return jdbc.sql("SELECT * FROM reports WHERE id = ? FOR UPDATE").param(id).query((row, index) ->
                new Report(id, row.getObject("work_id", UUID.class), row.getObject("reporter_id", UUID.class),
                        row.getString("reason"), instant(row, "created_at"), decision(row),
                        instant(row, "resolved_at"), row.getObject("moderator_id", UUID.class))).optional();
    }

    @Override
    public void update(Report report) {
        jdbc.sql("UPDATE reports SET decision = ?, resolved_at = ?, moderator_id = ? WHERE id = ?")
                .params(report.decision().name(), report.resolvedAt().atOffset(ZoneOffset.UTC),
                        report.moderatorId(), report.id()).update();
    }

    @Override
    public List<ModerationQueries.Summary> list(boolean resolved) {
        String condition = resolved ? "r.decision IS NOT NULL" : "r.decision IS NULL";
        String order = resolved ? "r.resolved_at DESC, r.id DESC LIMIT 100" : "r.created_at, r.id";
        return jdbc.sql("""
                SELECT r.*, w.title, a.pen_name FROM reports r
                JOIN works w ON w.id = r.work_id
                JOIN accounts a ON a.id = w.author_id
                WHERE
                """ + condition + " ORDER BY " + order).query((row, index) -> new ModerationQueries.Summary(
                row.getObject("id", UUID.class), row.getObject("work_id", UUID.class),
                row.getString("title"), row.getString("pen_name"), row.getString("reason"),
                instant(row, "created_at"), decision(row), instant(row, "resolved_at"))).list();
    }

    private static Instant instant(ResultSet row, String column) throws SQLException {
        var value = row.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    private static Report.Decision decision(ResultSet row) throws SQLException {
        String value = row.getString("decision");
        return value == null ? null : Report.Decision.valueOf(value);
    }
}
