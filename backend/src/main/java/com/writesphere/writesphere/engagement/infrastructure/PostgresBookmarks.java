package com.writesphere.writesphere.engagement.infrastructure;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.engagement.application.Bookshelf;
import com.writesphere.writesphere.post.application.PublishedWorks;
import com.writesphere.writesphere.post.infrastructure.PostgresPublishedWorks;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresBookmarks implements Bookshelf.Bookmarks, Bookshelf.ReadModel {
    private final JdbcClient jdbc;

    public PostgresBookmarks(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addVisible(UUID accountId, UUID workId) {
        return jdbc.sql("""
                WITH visible AS (SELECT id FROM published_works WHERE id = :work),
                added AS (
                    INSERT INTO bookmarks (account_id, work_id)
                    SELECT :account, id FROM visible ON CONFLICT DO NOTHING
                )
                SELECT EXISTS (SELECT 1 FROM visible)
                """).param("account", accountId).param("work", workId).query(Boolean.class).single();
    }

    @Override
    public void remove(UUID accountId, UUID workId) {
        jdbc.sql("DELETE FROM bookmarks WHERE account_id = ? AND work_id = ?")
                .params(accountId, workId).update();
    }

    @Override
    public List<PublishedWorks.Summary> list(UUID accountId) {
        return jdbc.sql("""
                SELECT w.id, w.author_id, w.pen_name, w.genre, w.title, left(w.body, 201) AS excerpt, w.published_at
                FROM bookmarks b JOIN published_works w ON w.id = b.work_id
                WHERE b.account_id = ? ORDER BY b.saved_at DESC, b.work_id DESC
                """).param(accountId).query(PostgresPublishedWorks::summary).list();
    }

    @Override
    public boolean contains(UUID accountId, UUID workId) {
        return jdbc.sql("""
                SELECT EXISTS (
                    SELECT 1 FROM bookmarks b JOIN published_works w ON w.id = b.work_id
                    WHERE b.account_id = ? AND b.work_id = ?
                )
                """).params(accountId, workId).query(Boolean.class).single();
    }
}
