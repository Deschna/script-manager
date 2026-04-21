package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScriptExecutionResponseMapperTest {

    private static final UUID EXECUTION_ID = UUID.fromString(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );
    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "warning\n";
    private static final Instant CREATED_AT = Instant.parse("2026-04-21T12:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-04-21T12:00:01Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-04-21T12:00:02Z");

    private final ScriptExecutionResponseMapper mapper = new ScriptExecutionResponseMapper();

    @Test
    void shouldMapScriptExecutionToResponse() {
        ScriptExecution execution = createCompletedExecution();

        ScriptExecutionResponse response = mapper.toResponse(execution);

        assertThat(response.id()).isEqualTo(EXECUTION_ID);
        assertThat(response.sourceCode()).isEqualTo(SOURCE_CODE);
        assertThat(response.status()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(response.stdout()).isEqualTo(STDOUT);
        assertThat(response.stderr()).isEqualTo(STDERR);
        assertThat(response.stackTrace()).isNull();
        assertThat(response.createdAt()).isEqualTo(CREATED_AT);
        assertThat(response.startedAt()).isEqualTo(STARTED_AT);
        assertThat(response.completedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void shouldMapScriptExecutionPageToResponse() {
        ScriptExecution execution = createCompletedExecution();
        ScriptExecutionPage page = new ScriptExecutionPage(
                List.of(execution),
                0,
                20,
                1,
                1
        );

        ScriptExecutionPageResponse response = mapper.toPageResponse(page);

        assertThat(response.content())
                .singleElement()
                .extracting(ScriptExecutionResponse::id)
                .isEqualTo(EXECUTION_ID);
        assertThat(response.pageNumber()).isEqualTo(0);
        assertThat(response.pageSize()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
    }

    private static ScriptExecution createCompletedExecution() {
        return ScriptExecution.restore(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.COMPLETED,
                STDOUT,
                STDERR,
                null,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT
        );
    }
}
