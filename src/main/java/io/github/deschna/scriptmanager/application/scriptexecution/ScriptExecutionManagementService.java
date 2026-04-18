package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ScriptExecutionManagementService {

    private final ScriptExecutionRepository scriptExecutionRepository;

    public ScriptExecution getById(UUID executionId) {
        UUID validatedExecutionId = Objects.requireNonNull(
                executionId,
                "executionId must not be null"
        );

        return scriptExecutionRepository.findById(validatedExecutionId)
                .orElseThrow(() -> new ScriptExecutionNotFoundException(validatedExecutionId));
    }

    public ScriptExecutionPage getPage(
            int pageNumber,
            int pageSize
    ) {
        validatePageRequest(pageNumber, pageSize);
        return scriptExecutionRepository.findPage(pageNumber, pageSize);
    }

    public void deleteFinished(UUID executionId) {
        ScriptExecution scriptExecution = getById(executionId);

        if (!scriptExecution.isFinished()) {
            throw new ScriptExecutionDeletionNotAllowedException(
                    scriptExecution.getId(),
                    scriptExecution.getStatus()
            );
        }

        scriptExecutionRepository.deleteById(scriptExecution.getId());
    }

    private static void validatePageRequest(
            int pageNumber,
            int pageSize
    ) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must not be negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
    }
}
