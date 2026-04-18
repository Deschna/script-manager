package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.util.Objects;
import java.util.UUID;

public final class ScriptExecutionDeletionNotAllowedException extends RuntimeException {

    public ScriptExecutionDeletionNotAllowedException(
            UUID executionId,
            ScriptExecutionStatus status
    ) {
        super(
                "Script execution cannot be deleted: "
                        + Objects.requireNonNull(executionId)
                        + " has status "
                        + Objects.requireNonNull(status)
        );
    }
}
