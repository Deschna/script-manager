package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.time.Instant;
import java.util.UUID;

public record ScriptExecutionResponse(
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
}
