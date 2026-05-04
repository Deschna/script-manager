package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class ScriptExecutionEntityMapper {

    public ScriptExecutionEntity toEntity(ScriptExecution scriptExecution) {
        Objects.requireNonNull(scriptExecution, "scriptExecution must not be null");

        ScriptExecutionEntity entity = new ScriptExecutionEntity();
        entity.setId(scriptExecution.getId());
        entity.setSourceCode(scriptExecution.getSourceCode());
        entity.setStatus(scriptExecution.getStatus());
        entity.setStdout(scriptExecution.getStdout());
        entity.setStderr(scriptExecution.getStderr());
        entity.setStackTrace(scriptExecution.getStackTrace());
        entity.setCreatedAt(scriptExecution.getCreatedAt());
        entity.setStartedAt(scriptExecution.getStartedAt());
        entity.setCompletedAt(scriptExecution.getCompletedAt());
        return entity;
    }

    public ScriptExecution toDomain(ScriptExecutionEntity entity) {
        Objects.requireNonNull(entity, "entity must not be null");

        return ScriptExecution.restore(
                entity.getId(),
                entity.getSourceCode(),
                entity.getStatus(),
                entity.getStdout(),
                entity.getStderr(),
                entity.getStackTrace(),
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getCompletedAt()
        );
    }
}
