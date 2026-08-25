package dev.filippotaiuti.temperature.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import dev.filippotaiuti.temperature.dto.TemperatureMeasurementRequest;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import dev.filippotaiuti.temperature.entity.TemperatureUnit;
import dev.filippotaiuti.temperature.repository.TemperatureMeasurementRepository;

@ExtendWith(MockitoExtension.class)
class TemperatureMeasurementServiceTest {
    @Mock
    private TemperatureMeasurementRepository repository;

    @InjectMocks
    private TemperatureMeasurementService service;

    @Test
    void testSaveTemperatureMeasurement() {
        TemperatureMeasurementRequest request = new TemperatureMeasurementRequest(
            BigDecimal.valueOf(25.5),
            TemperatureUnit.CELSIUS,
            "sensor-001"
        );
    
        ArgumentCaptor<TemperatureMeasurement> captor =
                ArgumentCaptor.forClass(TemperatureMeasurement.class);

        service.save(request);
        
        verify(repository).save(captor.capture());

        TemperatureMeasurement saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(request.getTemperature(), saved.getTemperature());
        assertEquals(request.getUnit(), saved.getUnit());
        assertEquals(request.getSensorId(), saved.getSensorId());
        assertNotNull(saved.getMeasuredAt());
        assertNotNull(saved.getCreatedAt());
    }
}