package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import org.springframework.stereotype.Component;

@Component
public class ScriptExecutionResponseMapper {

    public ScriptExecutionResponse toResponse(ScriptExecution scriptExecution) {
        return new ScriptExecutionResponse(
                scriptExecution.getId(),
                scriptExecution.getSourceCode(),
                scriptExecution.getStatus(),
                scriptExecution.getStdout(),
                scriptExecution.getStderr(),
                scriptExecution.getStackTrace(),
                scriptExecution.getCreatedAt(),
                scriptExecution.getStartedAt(),
                scriptExecution.getCompletedAt()
        );
    }

    public ScriptExecutionPageResponse toPageResponse(ScriptExecutionPage page) {
        return new ScriptExecutionPageResponse(
                page.content().stream()
                        .map(this::toResponse)
                        .toList(),
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
