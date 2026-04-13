package io.github.deschna.scriptmanager.application.scriptexecution;

public record ExecutionResult(
        boolean successful,
        String stdout,
        String stderr,
        String stackTrace
) {

    public static ExecutionResult success(String stdout, String stderr) {
        return new ExecutionResult(true, stdout, stderr, null);
    }

    public static ExecutionResult failure(String stdout, String stderr, String stackTrace) {
        return new ExecutionResult(false, stdout, stderr, stackTrace);
    }
}
