package com.writesphere.writesphere.post.infrastructure;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.post.application.EditorQueries;
import com.writesphere.writesphere.post.application.EditorQueries.Detail;
import com.writesphere.writesphere.post.application.WorkCommands;
import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.user.infrastructure.SessionAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/editor/works")
public class EditorController {
    private final WorkCommands commands;
    private final EditorQueries queries;

    public EditorController(WorkCommands commands, EditorQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    record NewDraft(@NotNull Work.Genre genre) {}
    record Revision(@NotNull @PositiveOrZero Long version, @NotNull Work.Genre genre,
                    @NotNull @Size(max = 160) String title, @NotNull @Size(max = 40000) String body) {}
    record ChangeState(@NotNull @PositiveOrZero Long version) {}

    @GetMapping
    List<EditorQueries.Summary> list(@AuthenticationPrincipal SessionAccount author) {
        return queries.list(author.id());
    }

    @GetMapping("/{id}")
    Detail get(@AuthenticationPrincipal SessionAccount author, @PathVariable UUID id) {
        return queries.get(author.id(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Detail create(@AuthenticationPrincipal SessionAccount author, @Valid @RequestBody NewDraft request) {
        return Detail.from(commands.handle(new WorkCommands.CreateDraft(author.id(), request.genre())));
    }

    @PutMapping("/{id}")
    Detail revise(@AuthenticationPrincipal SessionAccount author, @PathVariable UUID id,
                  @Valid @RequestBody Revision request) {
        return Detail.from(commands.handle(new WorkCommands.ReviseWork(author.id(), id, request.version(),
                request.genre(), request.title(), request.body())));
    }

    @PostMapping("/{id}/publish")
    Detail publish(@AuthenticationPrincipal SessionAccount author, @PathVariable UUID id,
                   @Valid @RequestBody ChangeState request) {
        return Detail.from(commands.handle(new WorkCommands.PublishWork(author.id(), id, request.version())));
    }

    @PostMapping("/{id}/withdraw")
    Detail withdraw(@AuthenticationPrincipal SessionAccount author, @PathVariable UUID id,
                    @Valid @RequestBody ChangeState request) {
        return Detail.from(commands.handle(new WorkCommands.WithdrawWork(author.id(), id, request.version())));
    }
}
