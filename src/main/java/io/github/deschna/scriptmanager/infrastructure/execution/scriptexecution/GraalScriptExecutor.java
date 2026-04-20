package io.github.deschna.scriptmanager.infrastructure.execution.scriptexecution;

import io.github.deschna.scriptmanager.application.scriptexecution.ExecutionResult;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutor;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SandboxPolicy;
import org.springframework.stereotype.Component;

@Component
public class GraalScriptExecutor implements ScriptExecutor {

    private static final String JAVASCRIPT_LANGUAGE_ID = "js";

    @Override
    public ExecutionResult execute(String sourceCode) {
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();

        try (Context context = createSandboxedContextBuilder(stdoutBuffer, stderrBuffer).build()) {
            context.eval(JAVASCRIPT_LANGUAGE_ID, sourceCode);
            return ExecutionResult.success(
                    stdoutBuffer.toString(StandardCharsets.UTF_8),
                    stderrBuffer.toString(StandardCharsets.UTF_8)
            );
        } catch (PolyglotException exception) {
            return ExecutionResult.failure(
                    stdoutBuffer.toString(StandardCharsets.UTF_8),
                    stderrBuffer.toString(StandardCharsets.UTF_8),
                    toStackTrace(exception)
            );
        }
    }

    private static Context.Builder createSandboxedContextBuilder(
            OutputStream stdout,
            OutputStream stderr
    ) {
        return Context.newBuilder(JAVASCRIPT_LANGUAGE_ID)
                .sandbox(SandboxPolicy.CONSTRAINED)
                .option("engine.WarnInterpreterOnly", "false")
                .out(stdout)
                .err(stderr);
    }

    private static String toStackTrace(PolyglotException exception) {
        StringBuilder stackTrace = new StringBuilder(exception.getMessage());

        for (PolyglotException.StackFrame stackFrame : exception.getPolyglotStackTrace()) {
            if (stackFrame.isGuestFrame()) {
                stackTrace.append(System.lineSeparator());
                stackTrace.append(stackFrame);
            }
        }

        return stackTrace.toString();
    }
}
