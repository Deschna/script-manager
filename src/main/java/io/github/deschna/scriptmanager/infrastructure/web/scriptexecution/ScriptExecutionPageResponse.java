package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import java.util.List;
import java.util.Objects;

public record ScriptExecutionPageResponse(
        List<ScriptExecutionResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {

    public ScriptExecutionPageResponse {
        content = List.copyOf(Objects.requireNonNull(content));
    }
}
