package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionDeletionNotAllowedException;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionManagementService;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionNotFoundException;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionProcessingService;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import io.github.deschna.scriptmanager.infrastructure.web.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScriptExecutionController.class)
@Import({
        GlobalExceptionHandler.class,
        ScriptExecutionResponseMapper.class
})
@TestPropertySource(properties = {
        "logging.level.io.github.deschna.scriptmanager.infrastructure.web.error."
                + "GlobalExceptionHandler=OFF"
})
class ScriptExecutionControllerTest {

    private static final UUID EXECUTION_ID = UUID.fromString(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );
    private static final Instant CREATED_AT = Instant.parse("2026-04-13T10:15:30Z");
    private static final Instant STARTED_AT = CREATED_AT.plusSeconds(5);
    private static final Instant COMPLETED_AT = CREATED_AT.plusSeconds(10);
    private static final String SCRIPT_EXECUTIONS_PATH = "/script-executions";
    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String EXECUTE_SCRIPT_REQUEST_BODY = """
            {
              "sourceCode": "console.log('hello');"
            }
            """;
    private static final String STDOUT = "hello\n";
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScriptExecutionProcessingService scriptExecutionProcessingService;

    @MockitoBean
    private ScriptExecutionManagementService scriptExecutionManagementService;

    @Test
    void shouldExecuteScriptAndReturnCreatedExecution() throws Exception {
        ScriptExecution execution = createCompletedExecution();

        when(scriptExecutionProcessingService.execute(SOURCE_CODE))
                .thenReturn(execution);

        mockMvc.perform(post(SCRIPT_EXECUTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EXECUTE_SCRIPT_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "http://localhost" + SCRIPT_EXECUTIONS_PATH + "/" + EXECUTION_ID
                ))
                .andExpect(jsonPath("$.id").value(EXECUTION_ID.toString()))
                .andExpect(jsonPath("$.sourceCode").value(SOURCE_CODE))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.stdout").value(STDOUT));

        verify(scriptExecutionProcessingService).execute(SOURCE_CODE);
    }

    @Test
    void shouldReturnExecutionById() throws Exception {
        ScriptExecution execution = createCompletedExecution();

        when(scriptExecutionManagementService.getById(EXECUTION_ID))
                .thenReturn(execution);

        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXECUTION_ID.toString()))
                .andExpect(jsonPath("$.sourceCode").value(SOURCE_CODE))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(scriptExecutionManagementService).getById(EXECUTION_ID);
    }

    @Test
    void shouldReturnExecutionPage() throws Exception {
        ScriptExecutionPage page = createPage(10);

        when(scriptExecutionManagementService.getPage(0, 10))
                .thenReturn(page);

        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(EXECUTION_ID.toString()))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(scriptExecutionManagementService).getPage(0, 10);
    }

    @Test
    void shouldReturnExecutionPageWithDefaultRequestParameters() throws Exception {
        ScriptExecutionPage page = createPage(DEFAULT_PAGE_SIZE);

        when(scriptExecutionManagementService.getPage(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE))
                .thenReturn(page);

        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(EXECUTION_ID.toString()))
                .andExpect(jsonPath("$.pageNumber").value(DEFAULT_PAGE_NUMBER))
                .andExpect(jsonPath("$.pageSize").value(DEFAULT_PAGE_SIZE))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(scriptExecutionManagementService).getPage(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
    }

    @Test
    void shouldReturnNoContentWhenDeletionSucceeds() throws Exception {
        mockMvc.perform(delete(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isNoContent());

        verify(scriptExecutionManagementService).deleteFinished(EXECUTION_ID);
    }

    @Test
    void shouldReturnNotFoundWhenExecutionDoesNotExist() throws Exception {
        when(scriptExecutionManagementService.getById(EXECUTION_ID))
                .thenThrow(new ScriptExecutionNotFoundException(EXECUTION_ID));

        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(
                        "Script execution not found: " + EXECUTION_ID
                ));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingExecutionThatDoesNotExist() throws Exception {
        doThrow(new ScriptExecutionNotFoundException(EXECUTION_ID))
                .when(scriptExecutionManagementService)
                .deleteFinished(EXECUTION_ID);

        mockMvc.perform(delete(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(
                        "Script execution not found: " + EXECUTION_ID
                ));

        verify(scriptExecutionManagementService).deleteFinished(EXECUTION_ID);
    }

    @Test
    void shouldReturnConflictWhenDeletionIsNotAllowed() throws Exception {
        doThrow(new ScriptExecutionDeletionNotAllowedException(
                EXECUTION_ID,
                ScriptExecutionStatus.EXECUTING
        ))
                .when(scriptExecutionManagementService)
                .deleteFinished(EXECUTION_ID);

        mockMvc.perform(delete(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(
                        "Script execution cannot be deleted: "
                                + EXECUTION_ID
                                + " has status EXECUTING"
                                + "; only finished executions can be deleted"
                ));
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyValidationFails() throws Exception {
        mockMvc.perform(post(SCRIPT_EXECUTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceCode": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("sourceCode"))
                .andExpect(jsonPath("$.errors[0].description").value(
                        "sourceCode must not be blank"
                ));

        verifyNoInteractions(scriptExecutionProcessingService);
    }

    @Test
    void shouldReturnBadRequestWhenSourceCodeExceedsMaximumLength() throws Exception {
        String oversizedSourceCode = "a".repeat(ExecuteScriptRequest.MAX_SOURCE_CODE_LENGTH + 1);

        mockMvc.perform(post(SCRIPT_EXECUTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyWithSourceCode(oversizedSourceCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("sourceCode"))
                .andExpect(jsonPath("$.errors[0].description").value(
                        "sourceCode must not exceed 10 000 characters"
                ));

        verifyNoInteractions(scriptExecutionProcessingService);
    }

    @Test
    void shouldReturnBadRequestWhenPageRequestParameterValidationFails() throws Exception {
        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("page"))
                .andExpect(jsonPath("$.errors[0].description").value(
                        "page must be greater than or equal to 0"
                ));

        verifyNoInteractions(scriptExecutionManagementService);
    }

    @Test
    void shouldReturnBadRequestWhenSizeRequestParameterValidationFails() throws Exception {
        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH)
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.errors[0].description").value(
                        "size must be greater than or equal to 1"
                ));

        verifyNoInteractions(scriptExecutionManagementService);
    }

    @Test
    void shouldReturnBadRequestWhenSizeRequestParameterExceedsMaximum() throws Exception {
        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH)
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.errors[0].description").value(
                        "size must be less than or equal to 100"
                ));

        verifyNoInteractions(scriptExecutionManagementService);
    }

    @Test
    void shouldReturnBadRequestWhenPathVariableHasInvalidFormat() throws Exception {
        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH + "/{executionId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid request value: executionId"));

        verifyNoInteractions(scriptExecutionManagementService);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post(SCRIPT_EXECUTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceCode":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Request body is malformed or unreadable"));

        verifyNoInteractions(scriptExecutionProcessingService);
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedErrorOccurs() throws Exception {
        when(scriptExecutionProcessingService.execute(SOURCE_CODE))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post(SCRIPT_EXECUTIONS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EXECUTE_SCRIPT_REQUEST_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Internal server error"));
    }

    private static ScriptExecution createCompletedExecution() {
        return ScriptExecution.restore(
                EXECUTION_ID,
                SOURCE_CODE,
                ScriptExecutionStatus.COMPLETED,
                STDOUT,
                "",
                null,
                CREATED_AT,
                STARTED_AT,
                COMPLETED_AT
        );
    }

    private static ScriptExecutionPage createPage(int pageSize) {
        return new ScriptExecutionPage(
                List.of(createCompletedExecution()),
                0,
                pageSize,
                1,
                1
        );
    }

    private static String requestBodyWithSourceCode(String sourceCode) {
        return """
                {
                  "sourceCode": "%s"
                }
                """.formatted(sourceCode);
    }
}
