package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionRepository;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScriptExecutionJpaRepositoryAdapter implements ScriptExecutionRepository {

    private static final Sort SCRIPT_EXECUTION_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

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

    @Override
    public ScriptExecutionPage findPage(
            int pageNumber,
            int pageSize
    ) {
        Page<ScriptExecutionEntity> page = scriptExecutionJpaRepository.findAll(
                PageRequest.of(
                        pageNumber,
                        pageSize,
                        SCRIPT_EXECUTION_SORT
                )
        );

        return new ScriptExecutionPage(
                page.getContent().stream()
                        .map(scriptExecutionMapper::toDomain)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
