package com.writesphere.writesphere.post.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.writesphere.writesphere.post.application.PublishedWorks;
import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicWorksController {
    private final PublishedWorks queries;

    public PublicWorksController(PublishedWorks queries) {
        this.queries = queries;
    }

    @GetMapping("/works")
    PublishedWorks.Page list(@RequestParam(required = false) Work.Genre genre,
                             @RequestParam(required = false) Instant before,
                             @RequestParam(required = false) UUID beforeId) {
        return queries.list(genre, cursor(before, beforeId));
    }

    @GetMapping("/works/{id}")
    PublishedWorks.Detail get(@PathVariable UUID id) {
        return queries.get(id);
    }

    @GetMapping("/authors/{id}")
    PublishedWorks.Profile profile(@PathVariable UUID id, @RequestParam(required = false) Instant before,
                                   @RequestParam(required = false) UUID beforeId) {
        return queries.profile(id, cursor(before, beforeId));
    }

    private PublishedWorks.Cursor cursor(Instant before, UUID beforeId) {
        if ((before == null) != (beforeId == null)) {
            throw new ApplicationProblem(ApplicationProblem.Kind.INVALID, "invalid_cursor");
        }
        return before == null ? null : new PublishedWorks.Cursor(before, beforeId);
    }
}
