package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionRepository;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScriptExecutionJpaRepositoryAdapter implements ScriptExecutionRepository {

    private final ScriptExecutionJpaRepository scriptExecutionJpaRepository;
    private final ScriptExecutionMapper scriptExecutionMapper;

    @Override
    public ScriptExecution save(ScriptExecution scriptExecution) {
        ScriptExecutionEntity savedEntity = scriptExecutionJpaRepository.save(
                scriptExecutionMapper.toEntity(scriptExecution)
        );
        return scriptExecutionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ScriptExecution> findById(UUID id) {
        return scriptExecutionJpaRepository.findById(id)
                .map(scriptExecutionMapper::toDomain);
    }
}
