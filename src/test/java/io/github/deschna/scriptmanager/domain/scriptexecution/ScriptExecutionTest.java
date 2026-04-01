package io.github.deschna.scriptmanager.domain.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ScriptExecutionTest {

    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";
    private static final String INVALID_SOURCE_CODE = "   ";

    @Test
    void shouldCreatePendingExecution() {
        Instant beforeCreation = Instant.now();
        ScriptExecution execution = ScriptExecution.create(SOURCE_CODE);
        Instant afterCreation = Instant.now();

        assertThat(execution.getId()).isNotNull();
        assertThat(execution.getSourceCode()).isEqualTo(SOURCE_CODE);
        assertThat(execution.getCreatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.PENDING);
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
        assertThat(execution.getStdout()).isNull();
        assertThat(execution.getStderr()).isNull();
        assertThat(execution.getStackTrace()).isNull();
    }

    @Test
    void shouldTransitionFromPendingToExecuting() {
        ScriptExecution execution = createExecution();

        Instant beforeStart = Instant.now();
        execution.start();
        Instant afterStart = Instant.now();

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.EXECUTING);
        assertThat(execution.getStartedAt()).isBetween(beforeStart, afterStart);
        assertThat(execution.getCompletedAt()).isNull();
        assertThat(execution.getStdout()).isNull();
        assertThat(execution.getStderr()).isNull();
        assertThat(execution.getStackTrace()).isNull();
    }

    @Test
    void shouldTransitionFromPendingToExecutingToCompleted() {
        ScriptExecution execution = createExecution();

        Instant beforeStart = Instant.now();
        execution.start();
        Instant afterStart = Instant.now();
        assertThat(execution.getStartedAt()).isBetween(beforeStart, afterStart);

        Instant beforeComplete = Instant.now();
        execution.complete(STDOUT, null);
        Instant afterComplete = Instant.now();
        assertThat(execution.getCompletedAt()).isBetween(beforeComplete, afterComplete);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(execution.getStdout()).isEqualTo(STDOUT);
        assertThat(execution.getStderr()).isEmpty();
        assertThat(execution.getStackTrace()).isNull();
    }

    @Test
    void shouldTransitionFromPendingToExecutingToFailed() {
        ScriptExecution execution = createExecution();

        Instant beforeStart = Instant.now();
        execution.start();
        Instant afterStart = Instant.now();
        assertThat(execution.getStartedAt()).isBetween(beforeStart, afterStart);

        Instant beforeFail = Instant.now();
        execution.fail(null, STDERR, STACK_TRACE);
        Instant afterFail = Instant.now();
        assertThat(execution.getCompletedAt()).isBetween(beforeFail, afterFail);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(STACK_TRACE);
    }

    @Test
    void shouldAllowFailedExecutionWithoutStackTrace() {
        ScriptExecution execution = createExecution();

        execution.start();
        execution.fail(null, STDERR, null);

        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStackTrace()).isNull();
    }

    @Test
    void shouldRejectBlankSourceCode() {
        assertThatThrownBy(() -> ScriptExecution.create(INVALID_SOURCE_CODE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectInvalidTransitionFromPendingToCompleted() {
        ScriptExecution execution = createExecution();
        assertPendingState(execution);

        assertThatThrownBy(() -> execution.complete(STDOUT, null))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertPendingState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromPendingToFailed() {
        ScriptExecution execution = createExecution();
        assertPendingState(execution);

        assertThatThrownBy(() -> execution.fail(null, STDERR, STACK_TRACE))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertPendingState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromExecutingToStart() {
        ScriptExecution execution = createExecution();
        execution.start();
        assertExecutingState(execution);

        assertThatThrownBy(execution::start)
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertExecutingState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromCompletedToStart() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.complete(STDOUT, null);
        assertCompletedState(execution);

        assertThatThrownBy(execution::start)
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertCompletedState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromCompletedToFailed() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.complete(STDOUT, null);
        assertCompletedState(execution);

        assertThatThrownBy(() -> execution.fail(null, STDERR, STACK_TRACE))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertCompletedState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromFailedToStart() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.fail(null, STDERR, STACK_TRACE);
        assertFailedState(execution, STACK_TRACE);

        assertThatThrownBy(execution::start)
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertFailedState(execution, STACK_TRACE);
    }

    @Test
    void shouldRejectInvalidTransitionFromFailedToComplete() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.fail(null, STDERR, STACK_TRACE);
        assertFailedState(execution, STACK_TRACE);

        assertThatThrownBy(() -> execution.complete(STDOUT, null))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertFailedState(execution, STACK_TRACE);
    }

    private static ScriptExecution createExecution() {
        return ScriptExecution.create(SOURCE_CODE);
    }

    private static void assertPendingState(ScriptExecution execution) {
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.PENDING);
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
        assertThat(execution.getStdout()).isNull();
        assertThat(execution.getStderr()).isNull();
        assertThat(execution.getStackTrace()).isNull();
    }

    private static void assertExecutingState(ScriptExecution execution) {
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.EXECUTING);
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNull();
        assertThat(execution.getStdout()).isNull();
        assertThat(execution.getStderr()).isNull();
        assertThat(execution.getStackTrace()).isNull();
    }

    private static void assertCompletedState(ScriptExecution execution) {
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.COMPLETED);
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNotNull();
        assertThat(execution.getStdout()).isEqualTo(STDOUT);
        assertThat(execution.getStderr()).isEmpty();
        assertThat(execution.getStackTrace()).isNull();
    }

    private static void assertFailedState(ScriptExecution execution, String expectedStackTrace) {
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNotNull();
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(expectedStackTrace);
    }
}
