package io.github.deschna.scriptmanager.domain.scriptexecution;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ScriptExecution {

    private static final Set<ScriptExecutionStatus> FINISHED_STATUSES = Set.of(
            ScriptExecutionStatus.COMPLETED,
            ScriptExecutionStatus.FAILED
    );

    private final UUID id;
    private final String sourceCode;
    private final Instant createdAt;

    private ScriptExecutionStatus status;
    private String stdout;
    private String stderr;
    private String stackTrace;
    private Instant startedAt;
    private Instant completedAt;

    private ScriptExecution(String sourceCode) {
        this.id = UUID.randomUUID();
        this.sourceCode = requireValidSourceCode(sourceCode);
        this.createdAt = Instant.now();
        this.status = ScriptExecutionStatus.PENDING;
    }

    private ScriptExecution(
            UUID id,
            String sourceCode,
            ScriptExecutionStatus status,
            String stdout,
            String stderr,
            String stackTrace,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {
        this.id = requireNonNull(id, "id");
        this.sourceCode = requireValidSourceCode(sourceCode);
        this.createdAt = requireNonNull(createdAt, "createdAt");
        this.status = requireNonNull(status, "status");
        this.stdout = stdout;
        this.stderr = stderr;
        this.stackTrace = stackTrace;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public static ScriptExecution create(String sourceCode) {
        return new ScriptExecution(sourceCode);
    }

    public static ScriptExecution restore(
            UUID id,
            String sourceCode,
            ScriptExecutionStatus status,
            String stdout,
            String stderr,
            String stackTrace,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {
        return new ScriptExecution(
                id,
                sourceCode,
                status,
                stdout,
                stderr,
                stackTrace,
                createdAt,
                startedAt,
                completedAt
        );
    }

    public void start() {
        ensureCurrentStatus(ScriptExecutionStatus.PENDING);
        this.status = ScriptExecutionStatus.EXECUTING;
        this.startedAt = Instant.now();
    }

    public void complete(String stdout, String stderr) {
        ensureCurrentStatus(ScriptExecutionStatus.EXECUTING);
        this.status = ScriptExecutionStatus.COMPLETED;
        this.stdout = normalizeOutput(stdout);
        this.stderr = normalizeOutput(stderr);
        this.completedAt = Instant.now();
    }

    public void fail(
            String stdout,
            String stderr,
            String stackTrace
    ) {
        ensureCurrentStatus(ScriptExecutionStatus.EXECUTING);
        this.status = ScriptExecutionStatus.FAILED;
        this.stdout = normalizeOutput(stdout);
        this.stderr = normalizeOutput(stderr);
        this.stackTrace = stackTrace;
        this.completedAt = Instant.now();
    }

    public boolean isFinished() {
        return FINISHED_STATUSES.contains(status);
    }

    private void ensureCurrentStatus(ScriptExecutionStatus expectedStatus) {
        if (status != expectedStatus) {
            throw new InvalidScriptExecutionStateTransitionException(
                    status,
                    expectedStatus
            );
        }
    }

    private static String normalizeOutput(String output) {
        return output == null ? "" : output;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }

    private static String requireValidSourceCode(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("sourceCode must not be blank");
        }
        return sourceCode;
    }
}
