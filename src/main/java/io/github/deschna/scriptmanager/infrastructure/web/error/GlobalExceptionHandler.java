package io.github.deschna.scriptmanager.infrastructure.web.error;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionDeletionNotAllowedException;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public final class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERRORS_PROPERTY = "errors";

    @ExceptionHandler(ScriptExecutionNotFoundException.class)
    public ProblemDetail handleNotFound(ScriptExecutionNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Script execution not found: " + exception.getExecutionId()
        );
    }

    @ExceptionHandler(ScriptExecutionDeletionNotAllowedException.class)
    public ProblemDetail handleConflict(ScriptExecutionDeletionNotAllowedException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Script execution cannot be deleted: "
                        + exception.getExecutionId()
                        + " has status "
                        + exception.getStatus()
                        + "; only finished executions can be deleted"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<ErrorDetail> errorDetails = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failure"
        );
        problemDetail.setProperty(ERRORS_PROPERTY, errorDetails);
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        List<ErrorDetail> errorDetails = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorDetail(
                        resolveViolationField(violation),
                        violation.getMessage()
                ))
                .toList();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failure"
        );
        problemDetail.setProperty(ERRORS_PROPERTY, errorDetails);
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid request value: " + exception.getName()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body is malformed or unreadable"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedError(Exception exception) {
        LOGGER.error("Unhandled exception during request processing", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    // For validation of controller method parameters, the violation path includes both
    // the method name and the invalid parameter name, for example "getPage.page".
    // The API field name is taken from the last path node.
    private static String resolveViolationField(ConstraintViolation<?> violation) {
        Path.Node lastNode = null;
        for (Path.Node node : violation.getPropertyPath()) {
            lastNode = node;
        }

        return lastNode == null ? null : lastNode.getName();
    }
}
