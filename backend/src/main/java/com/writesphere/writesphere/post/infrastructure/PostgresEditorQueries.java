package com.writesphere.writesphere.post.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.application.EditorQueries;
import com.writesphere.writesphere.post.domain.Work;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresEditorQueries implements EditorQueries.ReadModel {
    private final JdbcClient jdbc;

    public PostgresEditorQueries(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<EditorQueries.Summary> list(UUID authorId) {
        return jdbc.sql("""
                SELECT id, title, genre, state, updated_at FROM works
                WHERE author_id = ? ORDER BY updated_at DESC, id DESC
                """).param(authorId).query((row, index) -> new EditorQueries.Summary(
                row.getObject("id", UUID.class), row.getString("title"), Work.Genre.valueOf(row.getString("genre")),
                Work.State.valueOf(row.getString("state")), row.getObject("updated_at", OffsetDateTime.class).toInstant()
        )).list();
    }

    @Override
    public Optional<EditorQueries.Detail> find(UUID authorId, UUID workId) {
        return jdbc.sql("""
                SELECT id, title, body, genre, state, version FROM works WHERE author_id = ? AND id = ?
                """).params(authorId, workId).query((row, index) -> new EditorQueries.Detail(
                row.getObject("id", UUID.class), row.getString("title"), row.getString("body"),
                Work.Genre.valueOf(row.getString("genre")), Work.State.valueOf(row.getString("state")),
                row.getLong("version")
        )).optional();
    }
}
