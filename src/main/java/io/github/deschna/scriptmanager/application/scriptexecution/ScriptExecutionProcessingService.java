package io.github.deschna.scriptmanager.application.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class ScriptExecutionProcessingService {

    private final ScriptExecutionRepository scriptExecutionRepository;
    private final ScriptExecutor scriptExecutor;

    public ScriptExecution execute(String sourceCode) {
        ScriptExecution scriptExecution = ScriptExecution.create(sourceCode);

        scriptExecution.start();

        ExecutionResult executionResult = scriptExecutor.execute(sourceCode);
        applyExecutionResult(scriptExecution, executionResult);

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
