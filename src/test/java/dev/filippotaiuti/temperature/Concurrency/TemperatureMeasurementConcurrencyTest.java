package dev.filippotaiuti.temperature.Concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import dev.filippotaiuti.temperature.dto.MeasurementIngestionResult;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.MeasurementIngestionStatus;
import dev.filippotaiuti.temperature.entity.QuarantinedReason;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.repository.QuarantinedMeasurementRepository;
import dev.filippotaiuti.temperature.repository.SensorRepository;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;
import dev.filippotaiuti.temperature.service.TemperatureMeasurementService;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;

@SpringBootTest
@ActiveProfiles("local")
public class TemperatureMeasurementConcurrencyTest {

    @Autowired
    private TemperatureMeasurementService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TemperatureMeasurementRepository repository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private QuarantinedMeasurementRepository quarantinedRepository;

    @Test
    void testConcurrentIdenticalMeasurementsCreateOneQuarantineRow()
        throws Exception {

        // Arrange
        String sensorId = "concurrent-" + UUID.randomUUID();
        OffsetDateTime measuredAt = OffsetDateTime.parse("2026-10-30T08:00:00Z");

        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            new BigDecimal("24.0"),
            TemperatureUnit.CELSIUS,
            sensorId,
            "terminal-A",
            measuredAt);

        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger searches = new AtomicInteger();

        ProxyFactory proxyFactory = new ProxyFactory(quarantinedRepository);

        proxyFactory.addAdvice((MethodInterceptor) invocation -> {
            Object result = invocation.proceed();

            if (invocation.getMethod().getName().equals(
                "findBySensorIdAndTerminalIdAndMeasuredAtAndTemperatureAndUnit")) {

                if (searches.incrementAndGet() <= 2) {
                    assertTrue(((Optional<?>) result).isEmpty());
                    barrier.await(10, TimeUnit.SECONDS);
                }
            }

            return result;
        });

        QuarantinedMeasurementRepository coordinatedRepository = (QuarantinedMeasurementRepository) proxyFactory
            .getProxy();

        TemperatureMeasurementService service = new TemperatureMeasurementService(
            repository,
            sensorRepository,
            coordinatedRepository);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            // Act
            Future<MeasurementIngestionResult> first = executor.submit(() -> service.save(request));

            Future<MeasurementIngestionResult> second = executor.submit(() -> service.save(request));

            MeasurementIngestionResult firstResult = first.get(30, TimeUnit.SECONDS);

            MeasurementIngestionResult secondResult = second.get(30, TimeUnit.SECONDS);

            // Assert
            for (MeasurementIngestionResult result : List.of(firstResult, secondResult)) {
                assertEquals(
                    MeasurementIngestionStatus.QUARANTINED,
                    result.status());
                assertEquals(
                    QuarantinedReason.UNKNOWN_SENSOR,
                    result.reason());
                assertNull(result.measurement());
            }

            Long count = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM quarantined_measurements
                WHERE sensor_id = ?
                """, Long.class, sensorId);

            assertEquals(1L, count.longValue());

            // Verify: due ricerche iniziali e una rilettura dopo il duplicato.
            assertEquals(3, searches.get());

        } finally {
            executor.shutdownNow();

            assertTrue(
                executor.awaitTermination(10, TimeUnit.SECONDS),
                "I thread del test non sono terminati");

            jdbc.update(
                "DELETE FROM quarantined_measurements WHERE sensor_id = ?",
                sensorId);
        }
    }
}