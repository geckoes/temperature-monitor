CREATE TABLE sensors (
    sensor_id VARCHAR(100) PRIMARY KEY,
    terminal_id VARCHAR(100),
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT sensors_terminal_id_unique UNIQUE (terminal_id)
);