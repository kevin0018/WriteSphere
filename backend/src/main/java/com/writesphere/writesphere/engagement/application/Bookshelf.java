package com.writesphere.writesphere.engagement.application;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.post.application.PublishedWorks;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Bookshelf {
    private final Bookmarks bookmarks;
    private final ReadModel queries;

    public Bookshelf(Bookmarks bookmarks, ReadModel queries) {
        this.bookmarks = bookmarks;
        this.queries = queries;
    }

    public record AddBookmark(UUID accountId, UUID workId) {}
    public record RemoveBookmark(UUID accountId, UUID workId) {}

    public interface Bookmarks {
        boolean addVisible(UUID accountId, UUID workId);
        void remove(UUID accountId, UUID workId);
    }

    public interface ReadModel {
        List<PublishedWorks.Summary> list(UUID accountId);
        boolean contains(UUID accountId, UUID workId);
    }

    @Transactional
    public void handle(AddBookmark command) {
        if (!bookmarks.addVisible(command.accountId(), command.workId())) {
            throw new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "work_not_found");
        }
    }

    @Transactional
    public void handle(RemoveBookmark command) {
        bookmarks.remove(command.accountId(), command.workId());
    }

    public List<PublishedWorks.Summary> getMyBookshelf(UUID accountId) {
        return queries.list(accountId);
    }

    public boolean contains(UUID accountId, UUID workId) {
        return queries.contains(accountId, workId);
    }
}
