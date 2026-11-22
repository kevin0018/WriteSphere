package com.writesphere.writesphere.post.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.application.PublishedWorks;
import com.writesphere.writesphere.post.domain.Work;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresPublishedWorks implements PublishedWorks.ReadModel {
    private final JdbcClient jdbc;

    public PostgresPublishedWorks(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<PublishedWorks.Summary> list(UUID authorId, Work.Genre genre, PublishedWorks.Cursor before, int limit) {
        String sql = """
                SELECT id, author_id, pen_name, genre, title, left(body, 201) AS excerpt, published_at
                FROM published_works WHERE true
                """;
        if (authorId != null) sql += " AND author_id = :author";
        if (genre != null) sql += " AND genre = :genre";
        if (before != null) sql += " AND (published_at, id) < (:before, :id)";
        var query = jdbc.sql(sql + " ORDER BY published_at DESC, id DESC LIMIT :limit").param("limit", limit);
        if (authorId != null) query.param("author", authorId);
        if (genre != null) query.param("genre", genre.name());
        if (before != null) query.param("before", before.publishedAt().atOffset(ZoneOffset.UTC)).param("id", before.id());
        return query.query(PostgresPublishedWorks::summary).list();
    }

    @Override
    public Optional<PublishedWorks.Detail> find(UUID id) {
        return jdbc.sql("SELECT * FROM published_works WHERE id = ?").param(id).query((row, index) ->
                new PublishedWorks.Detail(id, row.getObject("author_id", UUID.class), row.getString("pen_name"),
                        Work.Genre.valueOf(row.getString("genre")), row.getString("title"), row.getString("body"),
                        publishedAt(row))).optional();
    }

    @Override
    public Optional<String> penName(UUID id) {
        return jdbc.sql("SELECT pen_name FROM accounts WHERE id = ?").param(id).query(String.class).optional();
    }

    public static PublishedWorks.Summary summary(ResultSet row, int index) throws SQLException {
        String excerpt = row.getString("excerpt");
        if (excerpt.codePointCount(0, excerpt.length()) > 200) {
            excerpt = excerpt.substring(0, excerpt.offsetByCodePoints(0, 200)).stripTrailing() + "…";
        }
        return new PublishedWorks.Summary(row.getObject("id", UUID.class),
                row.getObject("author_id", UUID.class), row.getString("pen_name"),
                Work.Genre.valueOf(row.getString("genre")), row.getString("title"), excerpt, publishedAt(row));
    }

    private static Instant publishedAt(ResultSet row) throws SQLException {
        return row.getObject("published_at", OffsetDateTime.class).toInstant();
    }
}
