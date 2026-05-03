package io.github.deschna.scriptmanager.application.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptExecutionProcessingServiceTest {

    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";

    @Mock
    private ScriptExecutionRepository scriptExecutionRepository;

    @Mock
    private ScriptExecutor scriptExecutor;

    private ScriptExecutionProcessingService scriptExecutionProcessingService;

    @BeforeEach
    void setUp() {
        scriptExecutionProcessingService = new ScriptExecutionProcessingService(
                scriptExecutionRepository,
                scriptExecutor
        );
    }

    @Test
    void shouldExecuteScriptAndPersistCompletedExecution() {
        givenRepositoryReturnsSavedExecution();
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenReturn(ExecutionResult.success(STDOUT, ""));

        ScriptExecution execution = scriptExecutionProcessingService.execute(SOURCE_CODE);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(execution.getStdout()).isEqualTo(STDOUT);
        assertThat(execution.getStderr()).isEmpty();
        assertThat(execution.getStackTrace()).isNull();

        InOrder inOrder = inOrder(scriptExecutor, scriptExecutionRepository);
        verifyExecutionFlow(inOrder, ScriptExecutionStatus.COMPLETED);
    }

    @Test
    void shouldPersistFailedExecutionWhenExecutorReturnsFailureResult() {
        givenRepositoryReturnsSavedExecution();
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenReturn(ExecutionResult.failure("", STDERR, STACK_TRACE));

        ScriptExecution execution = scriptExecutionProcessingService.execute(SOURCE_CODE);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(STACK_TRACE);

        InOrder inOrder = inOrder(scriptExecutor, scriptExecutionRepository);
        verifyExecutionFlow(inOrder, ScriptExecutionStatus.FAILED);
    }

    @Test
    void shouldNotPersistExecutionWhenExecutorFailsUnexpectedly() {
        IllegalStateException executorException = new IllegalStateException("Executor failure");

        when(scriptExecutor.execute(SOURCE_CODE))
                .thenThrow(executorException);

        assertThatThrownBy(() -> scriptExecutionProcessingService.execute(SOURCE_CODE))
                .isSameAs(executorException);

        verify(scriptExecutor).execute(SOURCE_CODE);
        verify(scriptExecutionRepository, never()).save(any(ScriptExecution.class));
    }

    @Test
    void shouldNotPersistExecutionWhenExecutorReturnsNull() {
        when(scriptExecutor.execute(SOURCE_CODE))
                .thenReturn(null);

        assertThatThrownBy(() -> scriptExecutionProcessingService.execute(SOURCE_CODE))
                .isInstanceOf(NullPointerException.class);

        verify(scriptExecutor).execute(SOURCE_CODE);
        verify(scriptExecutionRepository, never()).save(any(ScriptExecution.class));
    }

    private void givenRepositoryReturnsSavedExecution() {
        when(scriptExecutionRepository.save(any(ScriptExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void verifyExecutionFlow(InOrder inOrder, ScriptExecutionStatus expectedFinalStatus) {
        ArgumentCaptor<ScriptExecution> savedExecutionCaptor = ArgumentCaptor.forClass(
                ScriptExecution.class
        );

        inOrder.verify(scriptExecutor).execute(SOURCE_CODE);
        inOrder.verify(scriptExecutionRepository).save(savedExecutionCaptor.capture());

        assertThat(savedExecutionCaptor.getValue().getStatus()).isEqualTo(expectedFinalStatus);
    }
}
