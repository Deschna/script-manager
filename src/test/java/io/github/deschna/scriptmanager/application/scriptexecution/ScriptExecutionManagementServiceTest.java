package io.github.deschna.scriptmanager.application.scriptexecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScriptExecutionManagementServiceTest {

    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String STDOUT = "hello\n";
    private static final String STDERR = "boom";
    private static final String STACK_TRACE = "Error: boom\n    at <js>:1";

    @Mock
    private ScriptExecutionRepository scriptExecutionRepository;

    private ScriptExecutionManagementService scriptExecutionManagementService;

    @BeforeEach
    void setUp() {
        scriptExecutionManagementService = new ScriptExecutionManagementService(
                scriptExecutionRepository
        );
    }

    @Test
    void shouldReturnExecutionById() {
        ScriptExecution expectedExecution = ScriptExecution.create(SOURCE_CODE);

        when(scriptExecutionRepository.findById(expectedExecution.getId()))
                .thenReturn(Optional.of(expectedExecution));

        ScriptExecution actualExecution = scriptExecutionManagementService.getById(
                expectedExecution.getId()
        );

        assertThat(actualExecution).isSameAs(expectedExecution);

        verify(scriptExecutionRepository).findById(expectedExecution.getId());
    }

    @Test
    void shouldThrowTypedExceptionWhenExecutionIsMissing() {
        UUID missingExecutionId = UUID.randomUUID();

        when(scriptExecutionRepository.findById(missingExecutionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scriptExecutionManagementService.getById(missingExecutionId))
                .asInstanceOf(type(ScriptExecutionNotFoundException.class))
                .returns(missingExecutionId, ScriptExecutionNotFoundException::getExecutionId);

        verify(scriptExecutionRepository).findById(missingExecutionId);
    }

    @Test
    void shouldRejectNullExecutionId() {
        assertThatThrownBy(() -> scriptExecutionManagementService.getById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("executionId must not be null");

        verifyNoInteractions(scriptExecutionRepository);
    }

    @Test
    void shouldReturnRequestedPage() {
        ScriptExecutionPage expectedPage = new ScriptExecutionPage(
                List.of(
                        ScriptExecution.create(SOURCE_CODE),
                        ScriptExecution.create("console.log('bye');")
                ),
                1,
                2,
                5,
                3
        );

        when(scriptExecutionRepository.findPage(1, 2))
                .thenReturn(expectedPage);

        ScriptExecutionPage actualPage = scriptExecutionManagementService.getPage(1, 2);

        assertThat(actualPage).isEqualTo(expectedPage);

        verify(scriptExecutionRepository).findPage(1, 2);
    }

    @Test
    void shouldRejectNegativePageNumber() {
        assertThatThrownBy(() -> scriptExecutionManagementService.getPage(-1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pageNumber must not be negative");

        verifyNoInteractions(scriptExecutionRepository);
    }

    @Test
    void shouldRejectNonPositivePageSize() {
        assertThatThrownBy(() -> scriptExecutionManagementService.getPage(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pageSize must be greater than zero");

        verifyNoInteractions(scriptExecutionRepository);
    }

    @Test
    void shouldDeleteCompletedExecution() {
        ScriptExecution completedExecution = createCompletedExecution();

        when(scriptExecutionRepository.findById(completedExecution.getId()))
                .thenReturn(Optional.of(completedExecution));

        scriptExecutionManagementService.deleteFinished(completedExecution.getId());

        verify(scriptExecutionRepository).findById(completedExecution.getId());
        verify(scriptExecutionRepository).deleteById(completedExecution.getId());
    }

    @Test
    void shouldDeleteFailedExecution() {
        ScriptExecution failedExecution = createFailedExecution();

        when(scriptExecutionRepository.findById(failedExecution.getId()))
                .thenReturn(Optional.of(failedExecution));

        scriptExecutionManagementService.deleteFinished(failedExecution.getId());

        verify(scriptExecutionRepository).findById(failedExecution.getId());
        verify(scriptExecutionRepository).deleteById(failedExecution.getId());
    }

    @Test
    void shouldRejectDeletingPendingExecution() {
        ScriptExecution pendingExecution = createPendingExecution();

        when(scriptExecutionRepository.findById(pendingExecution.getId()))
                .thenReturn(Optional.of(pendingExecution));

        assertThatThrownBy(() ->
                scriptExecutionManagementService.deleteFinished(pendingExecution.getId()))
                .asInstanceOf(type(ScriptExecutionDeletionNotAllowedException.class))
                .returns(
                        pendingExecution.getId(),
                        ScriptExecutionDeletionNotAllowedException::getExecutionId
                )
                .returns(
                        ScriptExecutionStatus.PENDING,
                        ScriptExecutionDeletionNotAllowedException::getStatus
                );

        verify(scriptExecutionRepository).findById(pendingExecution.getId());
        verify(scriptExecutionRepository, never()).deleteById(pendingExecution.getId());
    }

    @Test
    void shouldRejectDeletingExecutingExecution() {
        ScriptExecution executingExecution = createExecutingExecution();

        when(scriptExecutionRepository.findById(executingExecution.getId()))
                .thenReturn(Optional.of(executingExecution));

        assertThatThrownBy(() ->
                scriptExecutionManagementService.deleteFinished(executingExecution.getId()))
                .asInstanceOf(type(ScriptExecutionDeletionNotAllowedException.class))
                .returns(
                        executingExecution.getId(),
                        ScriptExecutionDeletionNotAllowedException::getExecutionId
                )
                .returns(
                        ScriptExecutionStatus.EXECUTING,
                        ScriptExecutionDeletionNotAllowedException::getStatus
                );

        verify(scriptExecutionRepository).findById(executingExecution.getId());
        verify(scriptExecutionRepository, never()).deleteById(executingExecution.getId());
    }

    @Test
    void shouldThrowTypedExceptionWhenDeletingMissingExecution() {
        UUID missingExecutionId = UUID.randomUUID();

        when(scriptExecutionRepository.findById(missingExecutionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                scriptExecutionManagementService.deleteFinished(missingExecutionId))
                .asInstanceOf(type(ScriptExecutionNotFoundException.class))
                .returns(missingExecutionId, ScriptExecutionNotFoundException::getExecutionId);

        verify(scriptExecutionRepository).findById(missingExecutionId);
        verify(scriptExecutionRepository, never()).deleteById(missingExecutionId);
    }

    @Test
    void shouldRejectNullExecutionIdWhenDeleting() {
        assertThatThrownBy(() -> scriptExecutionManagementService.deleteFinished(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("executionId must not be null");

        verifyNoInteractions(scriptExecutionRepository);
    }

    private static ScriptExecution createPendingExecution() {
        return ScriptExecution.create(SOURCE_CODE);
    }

    private static ScriptExecution createExecutingExecution() {
        ScriptExecution scriptExecution = createPendingExecution();
        scriptExecution.start();
        return scriptExecution;
    }

    private static ScriptExecution createCompletedExecution() {
        ScriptExecution scriptExecution = createExecutingExecution();
        scriptExecution.complete(STDOUT, null);
        return scriptExecution;
    }

    private static ScriptExecution createFailedExecution() {
        ScriptExecution scriptExecution = createExecutingExecution();
        scriptExecution.fail(null, STDERR, STACK_TRACE);
        return scriptExecution;
    }
}
