package io.github.deschna.scriptmanager.domain.scriptexecution;

public final class InvalidScriptExecutionStateTransitionException extends RuntimeException {

    public InvalidScriptExecutionStateTransitionException(
            ScriptExecutionStatus currentStatus,
            ScriptExecutionStatus expectedStatus
    ) {
        super("Expected current script execution status %s but was %s"
                .formatted(expectedStatus, currentStatus));
    }
}
