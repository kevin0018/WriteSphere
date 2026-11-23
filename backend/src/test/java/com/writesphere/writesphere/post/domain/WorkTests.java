package com.writesphere.writesphere.post.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkTests {
    private static final Instant FIRST = Instant.parse("2026-11-22T12:00:00Z");
    private static final Instant LATER = FIRST.plusSeconds(60);

    @Test
    void requiresContentAndPreservesTheManuscriptAndFirstPublicationDate() {
        Work draft = Work.draft(UUID.randomUUID(), Work.Genre.POEM, FIRST);
        assertThatThrownBy(() -> draft.publish(FIRST)).hasMessage("work_incomplete");

        String poem = "Un verso\n\nUna estrofa\n";
        Work published = draft.revise(Work.Genre.POEM, " Título ", poem, FIRST).publish(FIRST);
        assertThat(published.title()).isEqualTo("Título");
        assertThat(published.body()).isEqualTo(poem);
        assertThatThrownBy(() -> published.revise(Work.Genre.POEM, "Título", " \n", LATER))
                .hasMessage("work_incomplete");

        Work republished = published.withdraw(LATER)
                .revise(Work.Genre.MICROFICTION, "Nueva versión", poem, LATER).publish(LATER);
        assertThat(republished.id()).isEqualTo(published.id());
        assertThat(republished.publishedAt()).isEqualTo(FIRST);
        assertThat(republished.updatedAt()).isEqualTo(LATER);
    }

    @Test
    void moderationPreventsAnAuthorFromReopeningAHiddenWork() {
        Work hidden = Work.draft(UUID.randomUUID(), Work.Genre.POEM, FIRST)
                .revise(Work.Genre.POEM, "Título", "Cuerpo", FIRST).publish(FIRST).hide(LATER);
        assertThat(hidden.state()).isEqualTo(Work.State.HIDDEN);
        assertThatThrownBy(() -> hidden.publish(LATER)).hasMessage("work_hidden");
        assertThatThrownBy(() -> hidden.withdraw(LATER)).hasMessage("work_hidden");
        assertThatThrownBy(() -> hidden.revise(Work.Genre.POEM, "Otro título", "Cuerpo", LATER))
                .hasMessage("work_hidden");
    }
}
