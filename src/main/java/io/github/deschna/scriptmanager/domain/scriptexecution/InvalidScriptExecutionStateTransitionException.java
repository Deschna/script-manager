package io.github.deschna.scriptmanager.domain.scriptexecution;

import java.util.Objects;
import lombok.Getter;

@Getter
public final class InvalidScriptExecutionStateTransitionException extends RuntimeException {

    private final ScriptExecutionStatus currentStatus;
    private final ScriptExecutionStatus expectedStatus;

    public InvalidScriptExecutionStateTransitionException(
            ScriptExecutionStatus currentStatus,
            ScriptExecutionStatus expectedStatus
    ) {
        super(buildMessage(currentStatus, expectedStatus));
        this.currentStatus = currentStatus;
        this.expectedStatus = expectedStatus;
    }

    private static String buildMessage(
            ScriptExecutionStatus currentStatus,
            ScriptExecutionStatus expectedStatus
    ) {
        return "Expected current script execution status "
                + Objects.requireNonNull(expectedStatus, "expectedStatus must not be null")
                + " but was "
                + Objects.requireNonNull(currentStatus, "currentStatus must not be null");
    }
}
