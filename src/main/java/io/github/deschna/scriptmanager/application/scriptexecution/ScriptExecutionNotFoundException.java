package io.github.deschna.scriptmanager.application.scriptexecution;

import java.util.Objects;
import java.util.UUID;

public final class ScriptExecutionNotFoundException extends RuntimeException {

    public ScriptExecutionNotFoundException(UUID executionId) {
        super("Script execution not found: " + Objects.requireNonNull(executionId));
    }
}
