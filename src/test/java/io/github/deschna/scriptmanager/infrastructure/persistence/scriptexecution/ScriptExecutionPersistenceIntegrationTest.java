package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(ScriptExecutionMapper.class)
class ScriptExecutionPersistenceIntegrationTest {

    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";

    @Autowired
    private ScriptExecutionJpaRepository scriptExecutionJpaRepository;

    @Autowired
    private ScriptExecutionMapper scriptExecutionMapper;

    @Test
    void shouldPersistAndRestoreFailedScriptExecution() {
        ScriptExecution expectedScriptExecution = ScriptExecution.create(SOURCE_CODE);
        expectedScriptExecution.start();
        expectedScriptExecution.fail(STDOUT, STDERR, STACK_TRACE);

        scriptExecutionJpaRepository.save(
                scriptExecutionMapper.toEntity(expectedScriptExecution)
        );

        ScriptExecutionEntity persistedEntity = scriptExecutionJpaRepository
                .findById(expectedScriptExecution.getId())
                .orElseThrow();

        assertThat(persistedEntity.getId()).isEqualTo(expectedScriptExecution.getId());
        assertThat(persistedEntity.getSourceCode())
                .isEqualTo(expectedScriptExecution.getSourceCode());
        assertThat(persistedEntity.getStatus()).isEqualTo(expectedScriptExecution.getStatus());
        assertThat(persistedEntity.getCreatedAt())
                .isEqualTo(expectedScriptExecution.getCreatedAt());
        assertThat(persistedEntity.getStartedAt())
                .isEqualTo(expectedScriptExecution.getStartedAt());
        assertThat(persistedEntity.getCompletedAt())
                .isEqualTo(expectedScriptExecution.getCompletedAt());
        assertThat(persistedEntity.getStdout())
                .isEqualTo(expectedScriptExecution.getStdout());
        assertThat(persistedEntity.getStderr()).isEqualTo(expectedScriptExecution.getStderr());
        assertThat(persistedEntity.getStackTrace())
                .isEqualTo(expectedScriptExecution.getStackTrace());

        ScriptExecution actualScriptExecution = scriptExecutionMapper.toDomain(persistedEntity);

        assertThat(actualScriptExecution.getId()).isEqualTo(expectedScriptExecution.getId());
        assertThat(actualScriptExecution.getSourceCode())
                .isEqualTo(expectedScriptExecution.getSourceCode());
        assertThat(actualScriptExecution.getStatus())
                .isEqualTo(expectedScriptExecution.getStatus());
        assertThat(actualScriptExecution.getCreatedAt())
                .isEqualTo(expectedScriptExecution.getCreatedAt());
        assertThat(actualScriptExecution.getStartedAt())
                .isEqualTo(expectedScriptExecution.getStartedAt());
        assertThat(actualScriptExecution.getCompletedAt())
                .isEqualTo(expectedScriptExecution.getCompletedAt());
        assertThat(actualScriptExecution.getStdout())
                .isEqualTo(expectedScriptExecution.getStdout());
        assertThat(actualScriptExecution.getStderr())
                .isEqualTo(expectedScriptExecution.getStderr());
        assertThat(actualScriptExecution.getStackTrace())
                .isEqualTo(expectedScriptExecution.getStackTrace());
    }
}
