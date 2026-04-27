package io.github.deschna.scriptmanager.application.scriptexecution;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ScriptExecutionNotFoundException extends RuntimeException {

    private final UUID executionId;

    public ScriptExecutionNotFoundException(UUID executionId) {
        super(buildMessage(executionId));
        this.executionId = executionId;
    }

    private static String buildMessage(UUID executionId) {
        return "Script execution not found: " + Objects.requireNonNull(executionId);
    }
}
