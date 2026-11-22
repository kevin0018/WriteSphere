package com.writesphere.writesphere.post.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import org.springframework.stereotype.Service;

@Service
public class PublishedWorks {
    private static final int PAGE_SIZE = 12;
    private final ReadModel works;

    public PublishedWorks(ReadModel works) {
        this.works = works;
    }

    public record Cursor(Instant publishedAt, UUID id) {}
    public record Summary(UUID id, UUID authorId, String penName, Work.Genre genre, String title,
                          String excerpt, Instant publishedAt) {}
    public record Detail(UUID id, UUID authorId, String penName, Work.Genre genre, String title,
                         String body, Instant publishedAt) {}
    public record Page(List<Summary> items, Cursor next) {}
    public record Profile(UUID id, String penName, Page works) {}

    public interface ReadModel {
        List<Summary> list(UUID authorId, Work.Genre genre, Cursor before, int limit);
        Optional<Detail> find(UUID id);
        Optional<String> penName(UUID id);
    }

    public Page list(Work.Genre genre, Cursor before) {
        return page(null, genre, before);
    }

    public Detail get(UUID id) {
        return works.find(id).orElseThrow(() ->
                new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "work_not_found"));
    }

    public Profile profile(UUID id, Cursor before) {
        String penName = works.penName(id).orElseThrow(() ->
                new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "author_not_found"));
        return new Profile(id, penName, page(id, null, before));
    }

    private Page page(UUID authorId, Work.Genre genre, Cursor before) {
        List<Summary> found = works.list(authorId, genre, before, PAGE_SIZE + 1);
        if (found.size() <= PAGE_SIZE) return new Page(found, null);
        var last = found.get(PAGE_SIZE - 1);
        return new Page(List.copyOf(found.subList(0, PAGE_SIZE)), new Cursor(last.publishedAt(), last.id()));
    }
}
