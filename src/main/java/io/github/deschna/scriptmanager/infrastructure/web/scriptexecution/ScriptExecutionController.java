package io.github.deschna.scriptmanager.infrastructure.web.scriptexecution;

import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionManagementService;
import io.github.deschna.scriptmanager.application.scriptexecution.ScriptExecutionProcessingService;
import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecution;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/script-executions")
@RequiredArgsConstructor
@Validated
public class ScriptExecutionController {

    private final ScriptExecutionProcessingService scriptExecutionProcessingService;
    private final ScriptExecutionManagementService scriptExecutionManagementService;
    private final ScriptExecutionResponseMapper scriptExecutionResponseMapper;

    @PostMapping
    public ResponseEntity<ScriptExecutionResponse> execute(
            @Valid @RequestBody ScriptExecutionRequest request
    ) {
        ScriptExecution execution = scriptExecutionProcessingService.execute(
                request.sourceCode()
        );
        ScriptExecutionResponse response = scriptExecutionResponseMapper.toResponse(execution);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{executionId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{executionId}")
    public ScriptExecutionResponse getById(@PathVariable UUID executionId) {
        return scriptExecutionResponseMapper.toResponse(
                scriptExecutionManagementService.getById(executionId)
        );
    }

    @GetMapping
    public ScriptExecutionPageResponse getPage(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size
    ) {
        return scriptExecutionResponseMapper.toPageResponse(
                scriptExecutionManagementService.getPage(page, size)
        );
    }

    @DeleteMapping("/{executionId}")
    public ResponseEntity<Void> deleteFinished(@PathVariable UUID executionId) {
        scriptExecutionManagementService.deleteFinished(executionId);
        return ResponseEntity.noContent().build();
    }
}
