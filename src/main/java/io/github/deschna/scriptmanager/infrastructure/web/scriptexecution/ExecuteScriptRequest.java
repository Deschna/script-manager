package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExecuteScriptRequest(
        @NotBlank(message = "sourceCode must not be blank")
        @Size(
                max = ExecuteScriptRequest.MAX_SOURCE_CODE_LENGTH,
                message = "sourceCode must not exceed 10 000 characters"
        )
        String sourceCode
) {
    public static final int MAX_SOURCE_CODE_LENGTH = 10_000;
}
