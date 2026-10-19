INSERT INTO sensors (sensor_id, terminal_id, name, created_at)
SELECT DISTINCT
    m.sensor_id,
    NULL,
    NULL,
    CURRENT_TIMESTAMP
FROM temperature_measurements m
WHERE NOT EXISTS (
    SELECT 1
    FROM sensors s
    WHERE s.sensor_id = m.sensor_id
);