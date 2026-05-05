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
    void shouldReturnSuccessAndCaptureOutput() {
        ExecutionResult result = scriptExecutor.execute(SUCCESSFUL_SCRIPT);

        assertThat(result.successful()).isTrue();
        assertThat(result.stdout()).isEqualTo("hello\n");
        assertThat(result.stderr()).isEqualTo("warn\n");
        assertThat(result.stackTrace()).isNull();
    }

    @Test
    void shouldReturnFailureAndStackTraceWhenScriptThrowsError() {
        ExecutionResult result = scriptExecutor.execute(FAILED_SCRIPT);

        assertThat(result.successful()).isFalse();
        assertThat(result.stdout()).isEqualTo("before failure\n");
        assertThat(result.stderr()).isEqualTo("failure warning\n");
        assertThat(result.stackTrace()).contains("boom");
    }

    @Test
    void shouldRejectHostAccess() {
        ExecutionResult result = scriptExecutor.execute(HOST_ACCESS_SCRIPT);

        assertThat(result.successful()).isFalse();
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stackTrace()).isNotBlank();
    }
}
