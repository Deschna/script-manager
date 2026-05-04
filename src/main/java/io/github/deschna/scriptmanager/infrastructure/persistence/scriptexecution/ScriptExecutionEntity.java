package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import io.github.deschna.scriptmanager.domain.scriptexecution.ScriptExecutionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "script_executions")
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScriptExecutionEntity {

    @Id
    private UUID id;

    @Column(name = "source_code", nullable = false, columnDefinition = "text")
    private String sourceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ScriptExecutionStatus status;

    @Column(name = "stdout", columnDefinition = "text")
    private String stdout;

    @Column(name = "stderr", columnDefinition = "text")
    private String stderr;

    @Column(name = "stack_trace", columnDefinition = "text")
    private String stackTrace;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
