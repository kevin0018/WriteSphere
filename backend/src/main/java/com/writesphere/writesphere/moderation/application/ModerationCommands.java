package com.writesphere.writesphere.moderation.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.writesphere.writesphere.moderation.domain.Report;
import com.writesphere.writesphere.post.application.WorkStore;
import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import com.writesphere.writesphere.user.domain.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModerationCommands {
    private final Reports reports;
    private final WorkStore works;

    public ModerationCommands(Reports reports, WorkStore works) {
        this.reports = reports;
        this.works = works;
    }

    public record FileReport(UUID workId, UUID reporterId, String reason) {}
    public record ResolveReport(UUID reportId, UUID moderatorId, Account.Role role,
                                Report.Decision decision) {}

    public interface Reports {
        boolean insertForVisibleWork(Report report);
        Optional<Report> lock(UUID id);
        void update(Report report);
    }

    public void handle(FileReport command) {
        var report = Report.open(command.workId(), command.reporterId(), command.reason(), Instant.now());
        if (!reports.insertForVisibleWork(report)) {
            throw new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "work_not_found");
        }
    }

    public void handle(ResolveReport command) {
        if (command.role() != Account.Role.MODERATOR) {
            throw new ApplicationProblem(ApplicationProblem.Kind.FORBIDDEN, "moderator_required");
        }
        Report report = reports.lock(command.reportId()).orElseThrow(() ->
                new ApplicationProblem(ApplicationProblem.Kind.NOT_FOUND, "report_not_found"));
        if (report.isResolved()) {
            throw new ApplicationProblem(ApplicationProblem.Kind.CONFLICT, "report_resolved");
        }
        Instant now = Instant.now();
        if (command.decision() == Report.Decision.HIDE) {
            Work work = works.find(report.workId()).orElseThrow();
            if (work.state() != Work.State.HIDDEN) works.update(work.hide(now));
        }
        reports.update(report.resolve(command.decision(), command.moderatorId(), now));
    }
}
