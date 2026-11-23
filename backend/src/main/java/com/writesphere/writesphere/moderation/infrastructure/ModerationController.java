package com.writesphere.writesphere.moderation.infrastructure;

import java.util.List;
import java.util.UUID;

import com.writesphere.writesphere.moderation.application.ModerationCommands;
import com.writesphere.writesphere.moderation.application.ModerationQueries;
import com.writesphere.writesphere.moderation.domain.Report;
import com.writesphere.writesphere.user.infrastructure.SessionAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ModerationController {
    private final ModerationCommands commands;
    private final ModerationQueries queries;

    public ModerationController(ModerationCommands commands, ModerationQueries queries) {
        this.commands = commands;
        this.queries = queries;
    }

    record Reason(@NotBlank @Size(max = 1000) String reason) {}
    record Resolution(@NotNull Report.Decision decision) {}

    @PostMapping("/works/{id}/reports")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void file(@AuthenticationPrincipal SessionAccount reader, @PathVariable UUID id,
              @Valid @RequestBody Reason request) {
        commands.handle(new ModerationCommands.FileReport(id, reader.id(), request.reason()));
    }

    @GetMapping("/moderation/reports")
    List<ModerationQueries.Summary> list(@RequestParam(defaultValue = "false") boolean resolved) {
        return queries.list(resolved);
    }

    @PostMapping("/moderation/reports/{id}/resolve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void resolve(@AuthenticationPrincipal SessionAccount moderator, @PathVariable UUID id,
                 @Valid @RequestBody Resolution request) {
        commands.handle(new ModerationCommands.ResolveReport(id, moderator.id(), moderator.role(), request.decision()));
    }
}
