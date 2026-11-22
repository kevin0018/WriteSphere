package com.writesphere.writesphere.post.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import org.springframework.stereotype.Service;

@Service
public class EditorQueries {
    private final ReadModel readModel;

    public EditorQueries(ReadModel readModel) {
        this.readModel = readModel;
    }

    public record Summary(UUID id, String title, Work.Genre genre, Work.State state, Instant updatedAt) {}
    public record Detail(UUID id, String title, String body, Work.Genre genre, Work.State state, long version) {
        public static Detail from(Work work) {
            return new Detail(work.id(), work.title(), work.body(), work.genre(), work.state(), work.version());
        }
    }

    public interface ReadModel {
        List<Summary> list(UUID authorId);
        Optional<Detail> find(UUID authorId, UUID workId);
    }

    public List<Summary> list(UUID authorId) {
        return readModel.list(authorId);
    }

    public Detail get(UUID authorId, UUID workId) {
        return readModel.find(authorId, workId)
                .orElseThrow(() -> new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "work_not_found"));
    }
}
