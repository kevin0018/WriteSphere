package com.writesphere.writesphere.post.application;

import java.time.Instant;
import java.util.UUID;

import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkCommands {
    private final WorkStore works;

    public WorkCommands(WorkStore works) {
        this.works = works;
    }

    public record CreateDraft(UUID authorId, Work.Genre genre) {}
    public record ReviseWork(UUID authorId, UUID workId, long version, Work.Genre genre,
                             String title, String body) {}
    public record PublishWork(UUID authorId, UUID workId, long version) {}
    public record WithdrawWork(UUID authorId, UUID workId, long version) {}

    public Work handle(CreateDraft command) {
        return works.insert(Work.draft(command.authorId(), command.genre(), Instant.now()));
    }

    public Work handle(ReviseWork command) {
        Work work = editable(command.authorId(), command.workId(), command.version());
        return works.update(work.revise(command.genre(), command.title(), command.body(), Instant.now()));
    }

    public Work handle(PublishWork command) {
        Work work = editable(command.authorId(), command.workId(), command.version());
        return works.update(work.publish(Instant.now()));
    }

    public Work handle(WithdrawWork command) {
        Work work = editable(command.authorId(), command.workId(), command.version());
        return works.update(work.withdraw(Instant.now()));
    }

    private Work editable(UUID authorId, UUID workId, long version) {
        Work work = works.find(workId).filter(candidate -> candidate.authorId().equals(authorId))
                .orElseThrow(() -> new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "work_not_found"));
        if (work.version() != version) {
            throw new ApplicationProblem(ApplicationProblem.Kind.CONFLICT, "work_conflict");
        }
        return work;
    }
}
