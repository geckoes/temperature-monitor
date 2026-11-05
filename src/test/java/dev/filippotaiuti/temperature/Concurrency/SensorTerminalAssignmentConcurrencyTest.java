package dev.filippotaiuti.temperature.Concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.SensorTerminalAssignmentRequest;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.Sensor;
import dev.filippotaiuti.temperature.exception.TerminalAlreadyAssignedException;
import dev.filippotaiuti.temperature.repository.QuarantinedMeasurementRepository;
import dev.filippotaiuti.temperature.repository.SensorRepository;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;
import dev.filippotaiuti.temperature.service.SensorService;

@SpringBootTest
@ActiveProfiles("local")
public class SensorTerminalAssignmentConcurrencyTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SensorRepository repository;

    @Test
    void testConcurrentTerminalAssignmentsAllowOnlyOneSensor() throws Exception {
        // Arrange
        String sensorId1 = "concurrent-" + UUID.randomUUID();
        String sensorId2 = "concurrent-" + UUID.randomUUID();
        String terminalId = "terminal-" + UUID.randomUUID();
        String originalTerminal1 = "original-A-" + UUID.randomUUID();
        String originalTerminal2 = "original-B-" + UUID.randomUUID();

        Sensor sensor1 = new Sensor(
            sensorId1, originalTerminal1, null,
            OffsetDateTime.parse("2026-10-16T08:00:00Z"));

        Sensor sensor2 = new Sensor(
            sensorId2, originalTerminal2, null,
            OffsetDateTime.parse("2026-10-16T08:00:00Z"));

        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger searches = new AtomicInteger();

        ProxyFactory proxyFactory = new ProxyFactory(repository);

        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();

            if (invocation.getMethod().getName().equals(
                "existsByTerminalId")) {

                if (searches.incrementAndGet() <= 2) {
                    assertEquals(Boolean.FALSE, result);
                    barrier.await(10, TimeUnit.SECONDS);
                }
            }

            return result;
        });

        SensorRepository coordinatedRepository = (SensorRepository) proxyFactory.getProxy();

        SensorService service = new SensorService(coordinatedRepository);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Act
            repository.saveAndFlush(sensor1);
            repository.saveAndFlush(sensor2);

            SensorTerminalAssignmentRequest request = new SensorTerminalAssignmentRequest(terminalId);

            Future<Sensor> first = executor.submit(
                () -> service.assignTerminalToSensor(sensorId1, request));

            Future<Sensor> second = executor.submit(
                () -> service.assignTerminalToSensor(sensorId2, request));

            // Assert
            int successes = 0;
            int conflicts = 0;

            for (Future<Sensor> future : List.of(first, second)) {
                try {
                    Sensor result = future.get(30, TimeUnit.SECONDS);
                    assertEquals(terminalId, result.getTerminalId());
                    successes++;
                } catch (ExecutionException exception) {
                    assertInstanceOf(
                        TerminalAlreadyAssignedException.class,
                        exception.getCause());
                    conflicts++;
                }
            }

            assertEquals(1, successes);
            assertEquals(1, conflicts);

            Sensor stored1 = repository.findById(sensorId1).orElseThrow();
            Sensor stored2 = repository.findById(sensorId2).orElseThrow();

            boolean firstWon = terminalId.equals(stored1.getTerminalId());

            assertEquals(
                firstWon ? terminalId : originalTerminal1,
                stored1.getTerminalId());

            assertEquals(
                firstWon ? originalTerminal2 : terminalId,
                stored2.getTerminalId());

            Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM sensors
                WHERE terminal_id = ?
                """, Long.class, terminalId);

            assertEquals(1L, count.longValue());

        } finally {
            executor.shutdownNow();

            assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "I thread del test non sono terminati");

            jdbc.update(
                "DELETE FROM sensors WHERE sensor_id = ?",
                sensorId1);

            jdbc.update(
                "DELETE FROM sensors WHERE sensor_id = ?",
                sensorId2);

        }
    }
}
