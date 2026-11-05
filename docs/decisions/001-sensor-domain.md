# ADR 001: Sensor identity, terminal assignment and measurement ingestion

Status: Accepted for the M03 implementation
Date: 2026-11-05

## Context

A terminal can move or be reconfigured. The logical source of measurement history
must remain identifiable independently of the physical terminal. Offline samples
must preserve the configuration and event time that applied when they were taken.

Existing installations already contain measurements identified only by sensorId.
Migration must preserve those records without inventing terminal identities.

## Decisions

### Logical identity and current assignment

- sensorId is the immutable logical sensor identifier and database primary key.
  It is not a hardware identifier and need not encode a physical area.
- terminalId identifies the terminal that supplies measurements. The current
  assignment is unique across registered sensors.
- For this project, one terminal is assigned to one logical sensor at a time.
  Real hardware may support multiple probes; modelling terminal channels or
  multi-probe hardware is outside M03.
- A sensor has an optional descriptive name and a server-generated createdAt.
- Registration is explicit. Ingestion does not create sensors automatically.
- PUT /api/sensors/{sensorId}/terminal replaces only the current assignment.
  Repeating the same assignment succeeds. Sensor identity, name and creation
  time are preserved.

### Historical association

Accepted measurements reference sensors through a sensorId foreign key.
Their own terminalId stores the terminal origin submitted with that sample.

There is no composite foreign key to the current sensor/terminal pair. Such a
constraint would prevent reassignment or require changes to historical origin.
Current-pair validation is an ingestion service responsibility.

Measurement uniqueness is (sensorId, measuredAt), independent of the submitting
terminal. measuredAt is event time, distinct from the server persistence time.

V3 backfills distinct logical sensors from history. Names and terminal assignments
remain null where unknown. V5 adds nullable terminal origin without rewriting
historical samples. New requests require terminalId; fictitious defaults are
not used to make old records look like newly identified terminal samples.

Before reconfiguring an offline terminal, operators should drain its old buffer.
Each buffered sample must retain its own original sensorId, terminalId and
measuredAt. Delayed samples are checked against the current assignment; M03
does not reconstruct historical assignment intervals.

### Ingestion outcomes and precedence

The service returns an internal MeasurementIngestionResult:

- STORED: a new accepted historical sample.
- ALREADY_STORED: an identical sample already exists in history.
- QUARANTINED: a syntactically valid sample is retained separately with a reason.

An exact historical retry is recognized first. Equality uses numeric temperature
comparison, identical unit and identical terminalId. Changes to the current
assignment do not invalidate acknowledgements of already stored samples.

Next, an existing identical quarantine entry is reused without changing its
original reason or receivedAt. Quarantine identity consists of sensorId,
terminalId, measuredAt, temperature and unit, excluding reason and reception time.

A different sample for an existing history key takes precedence over registry
validation and is quarantined as MEASUREMENT_CONFLICT. Otherwise the service
checks UNKNOWN_SENSOR, TERMINAL_NOT_ASSIGNED and TERMINAL_MISMATCH, in that order.

Quarantine is independent storage without a sensor foreign key so that unknown
sources remain inspectable. Registry changes do not automatically reprocess
quarantine. Operator decisions about correction, retention, deletion and
reprocessing are deferred.

### HTTP contract

- POST /api/sensors: 201 with SensorResponse and Location.
- GET /api/sensors/{sensorId}: 200 with SensorResponse; 404 if absent.
- PUT /api/sensors/{sensorId}/terminal: 200 with updated SensorResponse;
  404 for an absent sensor and 409 for a terminal owned by another sensor.
- POST /api/measurements: 200 with TemperatureMeasurementResponse for both
  STORED and ALREADY_STORED; 200 with {status, reason} for QUARANTINED.
  STORED and ALREADY_STORED are not separate public response fields.
- Invalid request bodies are rejected before ingestion and are not quarantined.
  ProblemDetail carries HTTP status, application code and detail or field errors.

Identifiers are case-sensitive, at most 100 characters, and use ASCII letters,
digits, dot, underscore or hyphen. Sensor name is optional, at most 255 characters.

### Database constraints and tested concurrency

Preflight queries improve normal error handling; database constraints remain the
authority for uniqueness under concurrency.

The implementation recovers a failed measurement insertion by re-reading the
history key. Identical content returns ALREADY_STORED; different content enters
quarantine. If no matching history exists, the original persistence error escapes.

A failed quarantine insertion reuses an exact existing quarantine entry; if none
exists, the original error escapes. A failed terminal reassignment checks whether
the requested terminal became occupied and reports TerminalAlreadyAssignedException;
otherwise it propagates the original persistence error.

Current recovery relies on repository-managed transactions. The ingestion and
assignment services do not surround the failed write and subsequent read with
one transaction. Adding an outer transaction requires reconsidering rollback,
flush timing and recovery boundaries.

Real PostgreSQL tests use barriers after initial reads to exercise:
- identical quarantine insertions;
- identical accepted measurements;
- different samples with the same history key;
- two different sensors claiming the same available terminal.

Service integration tests also verify terminal reassignment persistence and
unchanged historical measurement origin. The reported local completion suite
contains 110 passing tests; CI is checked separately on the merge candidate.

## Consequences and limits

- No sensorId rename, terminal unassignment, assignment timeline or automatic
  reassessment of quarantine is provided.
- Validation uses the current registry, not configuration at measuredAt.
  Ingestion concurrent with reassignment has no serialized assignment snapshot.
- Concurrent updates to the same sensor have no optimistic version check or
  conditional-update contract; this case is outside the tested two-sensor claim.
- Concurrent registration is not covered by the reassignment recovery:
  registering two sensors with one terminal can surface a persistence exception.
  Registration uses save on an assigned sensorId; simultaneous registrations of
  the same sensorId are not guaranteed to behave as an atomic insert-only API.
- The sensor registry APIs currently have no authentication or authorization.
  Public deployment requires a separate access-control and deployment review.
- Missing-measurement alarms, machine-room notifications, terminal buffering,
  simulator, dashboard and operator workflows are outside this milestone.

## Completion mapping

The registry is persisted and queryable through DTO-based APIs; accepted samples
reference registered sensors; invalid references enter quarantine; migrations
preserve existing history; automated tests exercise valid, invalid and competing
requests. README documents the API, validation, ingestion outcomes and migrations.
