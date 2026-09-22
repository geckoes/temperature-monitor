package dev.filippotaiuti.temperature.mapper;

import java.util.List;

import org.springframework.data.domain.Page;

import dev.filippotaiuti.temperature.dto.PagedResponse;
import dev.filippotaiuti.temperature.dto.TemperatureMeasurementResponse;
import dev.filippotaiuti.temperature.entity.TemperatureMeasurement;
import org.springframework.stereotype.Component;

@Component 
public class TemperatureMeasurementResponseMapper
{
    public PagedResponse<TemperatureMeasurementResponse> toPagedResponse(
            Page<TemperatureMeasurement> page)
    {
        List<TemperatureMeasurementResponse> content =
                page.getContent()
                        .stream()
                        .map(TemperatureMeasurementResponse::from)
                        .toList();

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}