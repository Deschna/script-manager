package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionManagementService;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionPage;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionProcessingService;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ScriptExecutionControllerTest {

    private static final UUID EXECUTION_ID = UUID.fromString(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );
    private static final String SCRIPT_EXECUTIONS_PATH = "/script-executions";
    private static final String SOURCE_CODE = "console.log('hello');";
    private static final String EXECUTE_SCRIPT_REQUEST_BODY = """
            {
              "sourceCode": "console.log('hello');"
            }
            """;
    private static final String STDOUT = "hello\n";
    private static final Instant CREATED_AT = Instant.parse("2026-04-21T12:00:00Z");
    private static final Instant STARTED_AT = Instant.parse("2026-04-21T12:00:01Z");
    private static final Instant COMPLETED_AT = Instant.parse("2026-04-21T12:00:02Z");

    @Mock
    private ScriptExecutionProcessingService scriptExecutionProcessingService;

    @Mock
    private ScriptExecutionManagementService scriptExecutionManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ScriptExecutionController controller = new ScriptExecutionController(
                scriptExecutionProcessingService,
                scriptExecutionManagementService,
                new ScriptExecutionResponseMapper()
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldExecuteScriptAndReturnCreatedExecution() throws Exception {
        when(scriptExecutionProcessingService.execute(SOURCE_CODE))
                .thenReturn(createCompletedExecution());

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
        when(scriptExecutionManagementService.getById(EXECUTION_ID))
                .thenReturn(createCompletedExecution());

        mockMvc.perform(get(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EXECUTION_ID.toString()))
                .andExpect(jsonPath("$.sourceCode").value(SOURCE_CODE))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(scriptExecutionManagementService).getById(EXECUTION_ID);
    }

    @Test
    void shouldReturnExecutionPage() throws Exception {
        ScriptExecutionPage page = new ScriptExecutionPage(
                List.of(createCompletedExecution()),
                0,
                10,
                1,
                1
        );

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
    void shouldDeleteFinishedExecution() throws Exception {
        mockMvc.perform(delete(SCRIPT_EXECUTIONS_PATH + "/{executionId}", EXECUTION_ID))
                .andExpect(status().isNoContent());

        verify(scriptExecutionManagementService).deleteFinished(EXECUTION_ID);
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
}
