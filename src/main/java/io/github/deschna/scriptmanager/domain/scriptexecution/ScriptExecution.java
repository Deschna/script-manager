package io.github.deschna.scriptmanager.domain.scriptexecution;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ScriptExecution {

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
        if (sourceCode == null || sourceCode.isBlank()) {
            throw new IllegalArgumentException("sourceCode must not be blank");
        }
        this.sourceCode = sourceCode;
        this.createdAt = Instant.now();
        this.status = ScriptExecutionStatus.PENDING;
    }

    public static ScriptExecution create(String sourceCode) {
        return new ScriptExecution(sourceCode);
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
}
