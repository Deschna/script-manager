package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.Optional;
import java.util.UUID;

public interface ScriptExecutionRepository {

    ScriptExecution save(ScriptExecution scriptExecution);

    Optional<ScriptExecution> findById(UUID executionId);

    ScriptExecutionPage findPage(
            int pageNumber,
            int pageSize
    );

    void deleteById(UUID executionId);
}
