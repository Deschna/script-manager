package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class ScriptExecutionProcessingService {

    private static final String INTERNAL_APPLICATION_ERROR_MESSAGE = "Internal application error";

    private final ScriptExecutionRepository scriptExecutionRepository;
    private final ScriptExecutor scriptExecutor;

    public ScriptExecution execute(String sourceCode) {
        ScriptExecution scriptExecution = ScriptExecution.create(sourceCode);
        scriptExecution = scriptExecutionRepository.save(scriptExecution);

        scriptExecution.start();

        try {
            ExecutionResult executionResult = scriptExecutor.execute(sourceCode);
            applyExecutionResult(scriptExecution, executionResult);
        } catch (RuntimeException exception) {
            scriptExecution.fail(null, INTERNAL_APPLICATION_ERROR_MESSAGE, null);
        }

        return scriptExecutionRepository.save(scriptExecution);
    }

    private static void applyExecutionResult(
            ScriptExecution scriptExecution,
            ExecutionResult executionResult
    ) {
        if (executionResult.successful()) {
            scriptExecution.complete(
                    executionResult.stdout(),
                    executionResult.stderr()
            );
        } else {
            scriptExecution.fail(
                    executionResult.stdout(),
                    executionResult.stderr(),
                    executionResult.stackTrace()
            );
        }
    }
}
