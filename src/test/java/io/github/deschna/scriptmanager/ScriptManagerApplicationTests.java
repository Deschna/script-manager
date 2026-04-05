package io.github.deschna.scriptmanager;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ScriptManagerApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldCreateScriptExecutionsTable() {
        Integer count = jdbcTemplate().queryForObject(
                "select count(*) from script_executions",
                Integer.class
        );

        assertThat(count).isZero();
    }

    @Test
    void shouldRegisterAppliedMigrationInFlywayHistory() {
        Integer appliedMigrationCount = jdbcTemplate().queryForObject(
                """
                select count(*)
                from flyway_schema_history
                where version = '1'
                  and success = true
                """,
                Integer.class
        );

        assertThat(appliedMigrationCount).isEqualTo(1);
    }

    private JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

}
