package com.writesphere.writesphere.post.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.writesphere.writesphere.post.domain.Work;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "works")
class WorkRow {
    @Id
    private UUID id;
    private UUID authorId;
    @Enumerated(EnumType.STRING)
    private Work.Genre genre;
    private String title;
    @Column(columnDefinition = "text")
    private String body;
    @Enumerated(EnumType.STRING)
    private Work.State state;
    @Version
    private long version;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;

    protected WorkRow() {}

    WorkRow(Work work) {
        id = work.id();
        authorId = work.authorId();
        createdAt = work.createdAt();
        apply(work);
    }

    void apply(Work work) {
        genre = work.genre();
        title = work.title();
        body = work.body();
        state = work.state();
        updatedAt = work.updatedAt();
        publishedAt = work.publishedAt();
    }

    Work toDomain() {
        return new Work(id, authorId, genre, title, body, state, version, createdAt, updatedAt, publishedAt);
    }
}
