package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionRepository;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        ScriptExecutionEntityMapper.class,
        ScriptExecutionRepositoryAdapter.class
})
class ScriptExecutionRepositoryAdapterIntegrationTest {

    private static final UUID PENDING_EXECUTION_ID = UUID.fromString(
            "dddddddd-dddd-dddd-dddd-dddddddddddd"
    );
    private static final UUID FAILED_EXECUTION_ID = UUID.fromString(
            "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"
    );
    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";
    private static final Instant CREATED_AT = Instant.parse("2026-04-13T10:15:30Z");
    private static final Instant STARTED_AT = CREATED_AT.plusSeconds(5);
    private static final Instant COMPLETED_AT = CREATED_AT.plusSeconds(10);

    @Autowired
    private ScriptExecutionRepository scriptExecutionRepository;

    @Test
    void shouldSaveAndFindFailedScriptExecution() {
        ScriptExecution expectedExecution = createFailedExecution();

        ScriptExecution savedExecution = scriptExecutionRepository.save(expectedExecution);

        ScriptExecution actualExecution = scriptExecutionRepository
                .findById(expectedExecution.getId())
                .orElseThrow();

        assertScriptExecution(savedExecution, expectedExecution);
        assertScriptExecution(actualExecution, expectedExecution);
    }

    @Test
    void shouldSaveAndFindPendingScriptExecution() {
        ScriptExecution expectedExecution = createPendingExecution();

        scriptExecutionRepository.save(expectedExecution);

        ScriptExecution actualExecution = scriptExecutionRepository
                .findById(expectedExecution.getId())
                .orElseThrow();

        assertScriptExecution(actualExecution, expectedExecution);
    }

    @Test
    void shouldReturnEmptyWhenExecutionIsMissing() {
        assertThat(scriptExecutionRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void shouldDeleteExecutionById() {
        ScriptExecution execution = createFailedExecution();
        scriptExecutionRepository.save(execution);

        scriptExecutionRepository.deleteById(execution.getId());

        assertThat(scriptExecutionRepository.findById(execution.getId())).isEmpty();
    }

    @Test
    void shouldReturnRequestedPageOrderedByCreatedAtDescAndIdDesc() {
        Instant olderCreatedAt = Instant.parse("2026-04-13T10:15:30Z");
        Instant newerCreatedAt = Instant.parse("2026-04-13T10:16:30Z");

        ScriptExecution oldestExecution = createCompletedExecution(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                olderCreatedAt
        );
        ScriptExecution sameTimestampLowerIdExecution = createCompletedExecution(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                newerCreatedAt
        );
        ScriptExecution sameTimestampHigherIdExecution = createCompletedExecution(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                newerCreatedAt
        );

        scriptExecutionRepository.save(oldestExecution);
        scriptExecutionRepository.save(sameTimestampLowerIdExecution);
        scriptExecutionRepository.save(sameTimestampHigherIdExecution);

        ScriptExecutionPage actualPage = scriptExecutionRepository.findPage(0, 2);

        assertThat(actualPage.pageNumber()).isEqualTo(0);
        assertThat(actualPage.pageSize()).isEqualTo(2);
        assertThat(actualPage.totalElements()).isEqualTo(3);
        assertThat(actualPage.totalPages()).isEqualTo(2);
        assertThat(actualPage.content())
                .extracting(ScriptExecution::getId)
                .containsExactly(
                        sameTimestampHigherIdExecution.getId(),
                        sameTimestampLowerIdExecution.getId()
                );
    }

    private static ScriptExecution createCompletedExecution(
            UUID id,
            Instant createdAt
    ) {
        return ScriptExecution.restore(
                id,
                SOURCE_CODE,
                ScriptExecutionStatus.COMPLETED,
                STDOUT,
                "",
                null,
                createdAt,
                createdAt.plusSeconds(5),
                createdAt.plusSeconds(10)
        );
    }

    private static ScriptExecution createPendingExecution() {
        return ScriptExecution.restore(
                PENDING_EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.PENDING,
                null,
                null,
                null,
                CREATED_AT,
                null,
                null
        );
    }

    private static ScriptExecution createFailedExecution() {
        return ScriptExecution.restore(
                FAILED_EXECUTION_ID,
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

    private static void assertScriptExecution(
            ScriptExecution actualExecution,
            ScriptExecution expectedExecution
    ) {
        assertThat(actualExecution.getId()).isEqualTo(expectedExecution.getId());
        assertThat(actualExecution.getSourceCode()).isEqualTo(expectedExecution.getSourceCode());
        assertThat(actualExecution.getStatus()).isEqualTo(expectedExecution.getStatus());
        assertThat(actualExecution.getStdout()).isEqualTo(expectedExecution.getStdout());
        assertThat(actualExecution.getStderr()).isEqualTo(expectedExecution.getStderr());
        assertThat(actualExecution.getStackTrace()).isEqualTo(expectedExecution.getStackTrace());
        assertThat(actualExecution.getCreatedAt()).isEqualTo(expectedExecution.getCreatedAt());
        assertThat(actualExecution.getStartedAt()).isEqualTo(expectedExecution.getStartedAt());
        assertThat(actualExecution.getCompletedAt()).isEqualTo(expectedExecution.getCompletedAt());
    }
}
