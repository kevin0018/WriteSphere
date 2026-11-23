package com.writesphere.writesphere.engagement.infrastructure;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.engagement.application.Bookshelf;
import com.writesphere.writesphere.post.application.PublishedWorks;
import com.writesphere.writesphere.user.infrastructure.SessionAccount;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookshelf")
public class BookshelfController {
    private final Bookshelf bookshelf;

    public BookshelfController(Bookshelf bookshelf) {
        this.bookshelf = bookshelf;
    }

    record Bookmark(boolean saved) {}

    @GetMapping
    List<PublishedWorks.Summary> list(@AuthenticationPrincipal SessionAccount account) {
        return bookshelf.getMyBookshelf(account.id());
    }

    @GetMapping("/{id}")
    Bookmark status(@AuthenticationPrincipal SessionAccount account, @PathVariable UUID id) {
        return new Bookmark(bookshelf.contains(account.id(), id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void add(@AuthenticationPrincipal SessionAccount account, @PathVariable UUID id) {
        bookshelf.handle(new Bookshelf.AddBookmark(account.id(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(@AuthenticationPrincipal SessionAccount account, @PathVariable UUID id) {
        bookshelf.handle(new Bookshelf.RemoveBookmark(account.id(), id));
    }
}
