package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScriptExecutionEntityMapperTest {

    private static final UUID EXECUTION_ID = UUID.fromString(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );
    private static final Instant CREATED_AT = Instant.parse("2026-04-13T10:15:30Z");
    private static final Instant STARTED_AT = CREATED_AT.plusSeconds(5);
    private static final Instant COMPLETED_AT = CREATED_AT.plusSeconds(10);
    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";

    private final ScriptExecutionEntityMapper mapper = new ScriptExecutionEntityMapper();

    @Test
    void shouldMapScriptExecutionToEntity() {
        ScriptExecution scriptExecution = createFailedExecution();

        ScriptExecutionEntity entity = mapper.toEntity(scriptExecution);

        assertFailedEntity(entity);
    }

    @Test
    void shouldMapEntityToScriptExecution() {
        ScriptExecutionEntity entity = createFailedEntity();

        ScriptExecution scriptExecution = mapper.toDomain(entity);

        assertFailedExecution(scriptExecution);
    }

    @Test
    void shouldMapCompletedExecutionWithoutStackTrace() {
        ScriptExecution scriptExecution = ScriptExecution.restore(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.COMPLETED,
                STDOUT,
                "",
                null,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT
        );

        ScriptExecutionEntity entity = mapper.toEntity(scriptExecution);
        ScriptExecution restoredScriptExecution = mapper.toDomain(entity);

        assertThat(restoredScriptExecution.getStatus()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(restoredScriptExecution.getStdout()).isEqualTo(STDOUT);
        assertThat(restoredScriptExecution.getStderr()).isEmpty();
        assertThat(restoredScriptExecution.getStackTrace()).isNull();
    }

    @Test
    void shouldRejectNullScriptExecution() {
        assertThatThrownBy(() -> mapper.toEntity(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("scriptExecution must not be null");
    }

    @Test
    void shouldRejectNullEntity() {
        assertThatThrownBy(() -> mapper.toDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("entity must not be null");
    }

    private static ScriptExecution createFailedExecution() {
        return ScriptExecution.restore(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.FAILED,
                STDOUT,
                STDERR,
                STACK_TRACE,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT
        );
    }

    private static ScriptExecutionEntity createFailedEntity() {
        ScriptExecutionEntity entity = new ScriptExecutionEntity();
        entity.setId(EXECUTION_ID);
        entity.setSourceCode(SOURCE_CODE);
        entity.setStatus(ScriptExecutionStatus.FAILED);
        entity.setStdout(STDOUT);
        entity.setStderr(STDERR);
        entity.setStackTrace(STACK_TRACE);
        entity.setCreatedAt(CREATED_AT);
        entity.setStartedAt(STARTED_AT);
        entity.setCompletedAt(COMPLETED_AT);
        return entity;
    }

    private static void assertFailedEntity(ScriptExecutionEntity entity) {
        assertThat(entity.getId()).isEqualTo(EXECUTION_ID);
        assertThat(entity.getSourceCode()).isEqualTo(SOURCE_CODE);
        assertThat(entity.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(entity.getStdout()).isEqualTo(STDOUT);
        assertThat(entity.getStderr()).isEqualTo(STDERR);
        assertThat(entity.getStackTrace()).isEqualTo(STACK_TRACE);
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(entity.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }

    private static void assertFailedExecution(ScriptExecution scriptExecution) {
        assertThat(scriptExecution.getId()).isEqualTo(EXECUTION_ID);
        assertThat(scriptExecution.getSourceCode()).isEqualTo(SOURCE_CODE);
        assertThat(scriptExecution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(scriptExecution.getStdout()).isEqualTo(STDOUT);
        assertThat(scriptExecution.getStderr()).isEqualTo(STDERR);
        assertThat(scriptExecution.getStackTrace()).isEqualTo(STACK_TRACE);
        assertThat(scriptExecution.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(scriptExecution.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(scriptExecution.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }
}
