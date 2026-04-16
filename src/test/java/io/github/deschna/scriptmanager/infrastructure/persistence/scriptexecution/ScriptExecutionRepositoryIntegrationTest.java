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
        ScriptExecutionMapper.class,
        ScriptExecutionJpaRepositoryAdapter.class
})
class ScriptExecutionRepositoryIntegrationTest {

    @Autowired
    private ScriptExecutionRepository scriptExecutionRepository;

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
                "console.log('hello');",
                ScriptExecutionStatus.COMPLETED,
                "hello\n",
                "",
                null,
                createdAt,
                createdAt.plusSeconds(5),
                createdAt.plusSeconds(10)
        );
    }
}
