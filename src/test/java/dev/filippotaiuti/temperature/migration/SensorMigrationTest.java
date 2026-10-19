package dev.filippotaiuti.temperature.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("local")
public class SensorMigrationTest {

    @Autowired
    private Environment environment;

    @Test
    void testMigrationPreservesExistingSensor() {
        String schema = "sensor_migration_" + UUID.randomUUID().toString().replace("-", "");

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            environment.getRequiredProperty("spring.datasource.url"),
            environment.getRequiredProperty("spring.datasource.username"),
            environment.getRequiredProperty("spring.datasource.password")
        );

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        try {
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .target("2")
                .load()
                .migrate();

            jdbc.execute("""
                INSERT INTO %s.sensors
                    (sensor_id, terminal_id, name, created_at)
                VALUES
                    ('sensor-001', 'terminal-A', 'South Warehouse', '2026-10-01T08:00:00Z')
                """.formatted(schema)
            );

            jdbc.execute("""
                INSERT INTO %s.temperature_measurements
                    (temperature, unit, sensor_id, measured_at)
                VALUES
                    (20.50, 'CELSIUS', 'sensor-001', '2026-10-01T08:00:00Z'),
                    (21.00, 'CELSIUS', 'sensor-001', '2026-10-01T09:00:00Z'),
                    (19.00, 'CELSIUS', 'sensor-002', '2026-10-01T08:00:00Z')
                """.formatted(schema)
            );

            String existingSensorQuery =
                "SELECT * FROM " + schema +
                ".sensors WHERE sensor_id = 'sensor-001'";

            Map<String, Object> sensorBefore = jdbc.queryForMap(existingSensorQuery);

            // Act
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .load()
                .migrate();

            // Assert
            assertEquals(List.of("sensor-001", "sensor-002"),
                jdbc.queryForList(
                    "SELECT sensor_id FROM " + schema +
                        ".sensors ORDER BY sensor_id",
                String.class));

            assertEquals(sensorBefore, jdbc.queryForMap(existingSensorQuery));

            Map<String, Object> migratedSensor = jdbc.queryForMap(
                    "SELECT * FROM " + schema +
                    ".sensors WHERE sensor_id = 'sensor-002'");

            assertNull(migratedSensor.get("terminal_id"));
            assertNull(migratedSensor.get("name"));
            assertNotNull(migratedSensor.get("created_at"));
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    @Test
    void testMigrateDistinctSensorsFromV2() {
        verifySensorMigrationFrom("2");
    }

    @Test
    void testMigrateDistinctSensorsFromV1() {
        verifySensorMigrationFrom("1");
    }

    private void verifySensorMigrationFrom(String initialVersion) {
        String schema = "sensor_migration_" + UUID.randomUUID().toString().replace("-", "");

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            environment.getRequiredProperty("spring.datasource.url"),
            environment.getRequiredProperty("spring.datasource.username"),
            environment.getRequiredProperty("spring.datasource.password")
        );

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        try {
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .target(initialVersion)
                .load()
                .migrate();

            jdbc.execute("""
                INSERT INTO %s.temperature_measurements
                    (temperature, unit, sensor_id, measured_at)
                VALUES
                    (20.50, 'CELSIUS', 'sensor-001', '2026-10-01T08:00:00Z'),
                    (21.00, 'CELSIUS', 'sensor-001', '2026-10-01T09:00:00Z'),
                    (19.00, 'CELSIUS', 'sensor-002', '2026-10-01T08:00:00Z')
                """.formatted(schema)
            );

            String measurementsQuery = "SELECT * FROM " + schema + ".temperature_measurements ORDER BY id";

            List<Map<String, Object>> measurementsBefore = jdbc.queryForList(measurementsQuery);

            // Act
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .locations("classpath:db/migration")
                .load()
                .migrate();

            // Assert
            List<String> sensorIds = jdbc.queryForList(
                "SELECT sensor_id FROM " + schema + ".sensors ORDER BY sensor_id",
                String.class
            );

            assertEquals(List.of("sensor-001", "sensor-002"), sensorIds);

            Integer sensorsWithoutMetadata = jdbc.queryForObject("""
                SELECT COUNT(*) FROM %s.sensors
                    WHERE terminal_id IS NULL
                    AND name IS NULL
                    AND created_at IS NOT NULL
                    """.formatted(schema),
                Integer.class);

            assertEquals(2, sensorsWithoutMetadata);

            List<Map<String, Object>> measurementsAfter = jdbc.queryForList(measurementsQuery);

            assertEquals(3, measurementsBefore.size());
            assertEquals(measurementsBefore, measurementsAfter);
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }
}
