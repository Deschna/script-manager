package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ScriptExecutionDeletionNotAllowedException extends RuntimeException {

    private final UUID executionId;
    private final ScriptExecutionStatus status;

    public ScriptExecutionDeletionNotAllowedException(
            UUID executionId,
            ScriptExecutionStatus status
    ) {
        super(buildMessage(executionId, status));
        this.executionId = executionId;
        this.status = status;
    }

    private static String buildMessage(UUID executionId, ScriptExecutionStatus status) {
        return "Script execution cannot be deleted: "
                + Objects.requireNonNull(executionId, "executionId must not be null")
                + " has status "
                + Objects.requireNonNull(status, "status must not be null");
    }
}
