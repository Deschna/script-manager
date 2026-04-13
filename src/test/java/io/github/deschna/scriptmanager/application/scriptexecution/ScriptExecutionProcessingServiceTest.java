package io.github.deschna.scriptmanager.application.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptExecutionProcessingServiceTest {

    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";
    private static final String INTERNAL_APPLICATION_ERROR_MESSAGE = "Internal application error";

    @Mock
    private ScriptExecutionRepository scriptExecutionRepository;

    @Mock
    private ScriptExecutor scriptExecutor;

    private final List<ScriptExecution> savedSnapshots = new ArrayList<>();

    private ScriptExecutionProcessingService scriptExecutionProcessingService;

    @BeforeEach
    void setUp() {
        savedSnapshots.clear();
        scriptExecutionProcessingService = new ScriptExecutionProcessingService(
                scriptExecutionRepository,
                scriptExecutor
        );

        when(scriptExecutionRepository.save(any(ScriptExecution.class)))
                .thenAnswer(invocation -> {
                    ScriptExecution scriptExecution = invocation.getArgument(0);
                    savedSnapshots.add(snapshotOf(scriptExecution));
                    return scriptExecution;
                });
    }

    @Test
    void shouldPersistPendingExecutionBeforeExecutingScriptAndReturnCompletedExecution() {
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenReturn(ExecutionResult.success(STDOUT, null));

        ScriptExecution execution = scriptExecutionProcessingService.execute(SOURCE_CODE);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(execution.getStdout()).isEqualTo(STDOUT);
        assertThat(execution.getStderr()).isEmpty();
        assertThat(execution.getStackTrace()).isNull();

        InOrder inOrder = inOrder(scriptExecutionRepository, scriptExecutor);
        assertExecutionFlow(inOrder, ScriptExecutionStatus.COMPLETED);
    }

    @Test
    void shouldPersistFailedExecutionWhenExecutorReturnsFailureResult() {
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenReturn(ExecutionResult.failure(null, STDERR, STACK_TRACE));

        ScriptExecution execution = scriptExecutionProcessingService.execute(SOURCE_CODE);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(STACK_TRACE);

        InOrder inOrder = inOrder(scriptExecutionRepository, scriptExecutor);
        assertExecutionFlow(inOrder, ScriptExecutionStatus.FAILED);
    }

    @Test
    void shouldPersistSanitizedFailureWhenExecutorThrowsUnexpectedException() {
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenThrow(new IllegalStateException("Executor failure"));

        ScriptExecution execution = scriptExecutionProcessingService.execute(SOURCE_CODE);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(INTERNAL_APPLICATION_ERROR_MESSAGE);
        assertThat(execution.getStackTrace()).isNull();

        InOrder inOrder = inOrder(scriptExecutionRepository, scriptExecutor);
        assertExecutionFlow(inOrder, ScriptExecutionStatus.FAILED);
    }

    private void assertExecutionFlow(
            InOrder inOrder,
            ScriptExecutionStatus expectedFinalStatus
    ) {
        inOrder.verify(scriptExecutionRepository).save(any(ScriptExecution.class));
        inOrder.verify(scriptExecutor).execute(SOURCE_CODE);
        inOrder.verify(scriptExecutionRepository).save(any(ScriptExecution.class));

        assertThat(savedSnapshots).hasSize(2);
        assertThat(savedSnapshots.get(0).getStatus()).isEqualTo(ScriptExecutionStatus.PENDING);
        assertThat(savedSnapshots.get(1).getStatus()).isEqualTo(expectedFinalStatus);
    }

    private static ScriptExecution snapshotOf(ScriptExecution scriptExecution) {
        return ScriptExecution.restore(
                scriptExecution.getId(),
                scriptExecution.getSourceCode(),
                scriptExecution.getStatus(),
                scriptExecution.getStdout(),
                scriptExecution.getStderr(),
                scriptExecution.getStackTrace(),
                scriptExecution.getCreatedAt(),
                scriptExecution.getStartedAt(),
                scriptExecution.getCompletedAt()
        );
    }
}
