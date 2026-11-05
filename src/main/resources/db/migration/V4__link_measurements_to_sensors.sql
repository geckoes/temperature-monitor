ALTER TABLE temperature_measurements
ADD CONSTRAINT temperature_measurements_sensor_fk
FOREIGN KEY (sensor_id)
REFERENCES sensors (sensor_id);