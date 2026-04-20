package io.github.deschna.scriptmanager.infrastructure.execution.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.deschna.scriptmanager.application.scriptexecution.ExecutionResult;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutor;
import org.junit.jupiter.api.Test;

class GraalScriptExecutorTest {

    private static final String SUCCESSFUL_SCRIPT = """
            console.log('hello');
            console.error('warn');
            """;
    private static final String FAILED_SCRIPT = """
            console.log('before failure');
            console.error('failure warning');
            throw new Error('boom');
            """;
    private static final String HOST_ACCESS_SCRIPT = """
            Java.type('java.lang.System').getenv();
            """;

    private final ScriptExecutor scriptExecutor = new GraalScriptExecutor();

    @Test
    void shouldExecuteScriptAndCaptureOutput() {
        ExecutionResult result = scriptExecutor.execute(SUCCESSFUL_SCRIPT);

        assertThat(result.successful()).isTrue();
        assertThat(result.stdout()).contains("hello");
        assertThat(result.stderr()).contains("warn");
        assertThat(result.stackTrace()).isNull();
    }

    @Test
    void shouldReturnFailureAndStackTraceWhenScriptThrowsError() {
        ExecutionResult result = scriptExecutor.execute(FAILED_SCRIPT);

        assertThat(result.successful()).isFalse();
        assertThat(result.stdout()).contains("before failure");
        assertThat(result.stderr()).contains("failure warning");
        assertThat(result.stackTrace()).contains("boom");
    }

    @Test
    void shouldRejectHostAccess() {
        ExecutionResult result = scriptExecutor.execute(HOST_ACCESS_SCRIPT);

        assertThat(result.successful()).isFalse();
        assertThat(result.stackTrace()).isNotBlank();
    }
}
