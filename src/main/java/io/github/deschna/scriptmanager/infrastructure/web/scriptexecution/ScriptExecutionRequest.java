package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import jakarta.validation.constraints.NotBlank;

public record ScriptExecutionRequest(
        @NotBlank(message = "sourceCode must not be blank")
        String sourceCode
) {
}
