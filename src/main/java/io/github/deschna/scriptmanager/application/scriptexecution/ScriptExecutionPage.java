package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.List;
import java.util.Objects;

public record ScriptExecutionPage(
        List<ScriptExecution> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {

    public ScriptExecutionPage {
        content = List.copyOf(Objects.requireNonNull(content));
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must not be negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
        if (totalPages < 0) {
            throw new IllegalArgumentException("totalPages must not be negative");
        }
    }
}
