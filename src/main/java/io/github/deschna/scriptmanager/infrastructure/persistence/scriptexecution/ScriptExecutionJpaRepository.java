package io.github.deschna.scriptmanager.infrastructure.persistence.scriptexecution;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScriptExecutionJpaRepository extends JpaRepository<ScriptExecutionEntity, UUID> {
}
