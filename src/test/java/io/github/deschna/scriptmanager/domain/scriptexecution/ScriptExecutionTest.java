package io.github.deschna.scriptmanager.domain.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScriptExecutionTest {

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
    void shouldRestoreExecution() {
        ScriptExecution execution = restoreExecution(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.FAILED
        );

        assertThat(execution.getId()).isEqualTo(EXECUTION_ID);
        assertThat(execution.getSourceCode()).isEqualTo(SOURCE_CODE);
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStdout()).isEqualTo(STDOUT);
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(STACK_TRACE);
        assertThat(execution.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(execution.getStartedAt()).isEqualTo(STARTED_AT);
        assertThat(execution.getCompletedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void shouldRejectRestoreWithoutId() {
        assertThatThrownBy(() -> restoreExecution(null, SOURCE_CODE, ScriptExecutionStatus.FAILED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id must not be null");
    }

    @Test
    void shouldRejectRestoreWithoutSourceCode() {
        assertThatThrownBy(() -> restoreExecution(EXECUTION_ID, null, ScriptExecutionStatus.FAILED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceCode must not be blank");
    }

    @Test
    void shouldRejectRestoreWithoutStatus() {
        assertThatThrownBy(() -> restoreExecution(EXECUTION_ID, SOURCE_CODE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("status must not be null");
    }

    @Test
    void shouldRejectRestoreWithoutCreatedAt() {
        assertThatThrownBy(() -> ScriptExecution.restore(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.FAILED,
                STDOUT,
                STDERR,
                STACK_TRACE,
                null,
                STARTED_AT,
                COMPLETED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("createdAt must not be null");
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
    void shouldIdentifyCompletedAndFailedExecutionsAsFinished() {
        ScriptExecution completedExecution = createExecution();
        completedExecution.start();
        completedExecution.complete(STDOUT, null);

        ScriptExecution failedExecution = createExecution();
        failedExecution.start();
        failedExecution.fail(null, STDERR, STACK_TRACE);

        assertThat(completedExecution.isFinished()).isTrue();
        assertThat(failedExecution.isFinished()).isTrue();
    }

    @Test
    void shouldIdentifyPendingAndExecutingExecutionsAsNotFinished() {
        ScriptExecution pendingExecution = createExecution();

        ScriptExecution executingExecution = createExecution();
        executingExecution.start();

        assertThat(pendingExecution.isFinished()).isFalse();
        assertThat(executingExecution.isFinished()).isFalse();
    }

    @Test
    void shouldRejectBlankSourceCode() {
        assertThatThrownBy(() -> ScriptExecution.create(INVALID_SOURCE_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceCode must not be blank");
    }

    @Test
    void shouldRejectNullSourceCode() {
        assertThatThrownBy(() -> ScriptExecution.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceCode must not be blank");
    }

    @Test
    void shouldRejectInvalidTransitionFromPendingToCompletedWithTransitionDetails() {
        ScriptExecution execution = createExecution();
        assertPendingState(execution);

        assertThatThrownBy(() -> execution.complete(STDOUT, null))
                .asInstanceOf(type(InvalidScriptExecutionStateTransitionException.class))
                .returns(
                        ScriptExecutionStatus.PENDING,
                        InvalidScriptExecutionStateTransitionException::getCurrentStatus
                )
                .returns(
                        ScriptExecutionStatus.EXECUTING,
                        InvalidScriptExecutionStateTransitionException::getExpectedStatus
                );

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
    void shouldRejectInvalidTransitionFromCompletedToCompleted() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.complete(STDOUT, null);
        assertCompletedState(execution);

        assertThatThrownBy(() -> execution.complete(STDOUT, null))
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
        assertFailedState(execution);

        assertThatThrownBy(execution::start)
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertFailedState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromFailedToComplete() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.fail(null, STDERR, STACK_TRACE);
        assertFailedState(execution);

        assertThatThrownBy(() -> execution.complete(STDOUT, null))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertFailedState(execution);
    }

    @Test
    void shouldRejectInvalidTransitionFromFailedToFailed() {
        ScriptExecution execution = createExecution();
        execution.start();
        execution.fail(null, STDERR, STACK_TRACE);
        assertFailedState(execution);

        assertThatThrownBy(() -> execution.fail(null, STDERR, STACK_TRACE))
                .isInstanceOf(InvalidScriptExecutionStateTransitionException.class);

        assertFailedState(execution);
    }

    private static ScriptExecution createExecution() {
        return ScriptExecution.create(SOURCE_CODE);
    }

    private static ScriptExecution restoreExecution(
            UUID id,
            String sourceCode,
            ScriptExecutionStatus status
    ) {
        return ScriptExecution.restore(
                id,
                sourceCode,
                status,
                STDOUT,
                STDERR,
                STACK_TRACE,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT
        );
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

    private static void assertFailedState(ScriptExecution execution) {
        assertThat(execution.getStatus()).isEqualTo(ScriptExecutionStatus.FAILED);
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNotNull();
        assertThat(execution.getStdout()).isEmpty();
        assertThat(execution.getStderr()).isEqualTo(STDERR);
        assertThat(execution.getStackTrace()).isEqualTo(STACK_TRACE);
    }
}
