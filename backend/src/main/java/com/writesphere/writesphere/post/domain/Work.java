package com.writesphere.writesphere.post.domain;

import java.time.Instant;
import java.util.UUID;

public record Work(UUID id, UUID authorId, Genre genre, String title, String body, State state,
                   long version, Instant createdAt, Instant updatedAt, Instant publishedAt) {
    public enum Genre { POEM, MICROFICTION }
    public enum State { DRAFT, PUBLISHED, HIDDEN }

    public static Work draft(UUID authorId, Genre genre, Instant now) {
        return new Work(UUID.randomUUID(), authorId, genre, "", "", State.DRAFT, 0, now, now, null);
    }

    public Work revise(Genre genre, String title, String body, Instant now) {
        requireEditable();
        if (state == State.PUBLISHED) requireContent(title, body);
        return new Work(id, authorId, genre, title.strip(), body, state, version, createdAt, now, publishedAt);
    }

    public Work publish(Instant now) {
        requireEditable();
        requireContent(title, body);
        return new Work(id, authorId, genre, title, body, State.PUBLISHED, version, createdAt, now,
                publishedAt == null ? now : publishedAt);
    }

    public Work withdraw(Instant now) {
        requireEditable();
        return new Work(id, authorId, genre, title, body, State.DRAFT, version, createdAt, now, publishedAt);
    }

    public Work hide(Instant now) {
        return new Work(id, authorId, genre, title, body, State.HIDDEN, version, createdAt, now, publishedAt);
    }

    private void requireEditable() {
        if (state == State.HIDDEN) throw new RuleViolation("work_hidden");
    }

    private static void requireContent(String title, String body) {
        if (title.isBlank() || body.isBlank()) throw new RuleViolation("work_incomplete");
    }

    public static final class RuleViolation extends RuntimeException {
        public RuleViolation(String code) {
            super(code);
        }
    }
}
