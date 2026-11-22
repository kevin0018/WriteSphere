package com.writesphere.writesphere.shared.infrastructure;

import java.util.Map;

import com.writesphere.writesphere.post.domain.Work;
import com.writesphere.writesphere.shared.application.ApplicationProblem;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiProblems {
    @ExceptionHandler(ApplicationProblem.class)
    ResponseEntity<Map<String, String>> handle(ApplicationProblem problem) {
        int status = switch (problem.kind()) {
            case INVALID -> 400;
            case NOT_FOUND -> 404;
            case CONFLICT -> 409;
        };
        return ResponseEntity.status(status).body(Map.of("code", problem.getMessage()));
    }

    @ExceptionHandler(Work.RuleViolation.class)
    ResponseEntity<Map<String, String>> handle(Work.RuleViolation problem) {
        return ResponseEntity.status(problem.getMessage().equals("work_hidden") ? 409 : 400)
                .body(Map.of("code", problem.getMessage()));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    ResponseEntity<Map<String, String>> conflict() {
        return ResponseEntity.status(409).body(Map.of("code", "work_conflict"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, String>> invalidInput() {
        return ResponseEntity.badRequest().body(Map.of("code", "invalid_input"));
    }
}
