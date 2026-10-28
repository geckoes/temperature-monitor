ALTER TABLE quarantined_measurements
ADD CONSTRAINT quarantined_measurements_sample_unique
UNIQUE (sensor_id, terminal_id, measured_at, temperature, unit);