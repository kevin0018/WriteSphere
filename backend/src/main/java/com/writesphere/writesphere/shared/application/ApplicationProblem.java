package com.writesphere.writesphere.shared.application;

public final class ApplicationProblem extends RuntimeException {
    public enum Kind { INVALID, FORBIDDEN, NOT_FOUND, CONFLICT }

    private final Kind kind;

    public ApplicationProblem(Kind kind, String code) {
        super(code);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
