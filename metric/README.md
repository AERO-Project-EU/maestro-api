# Metric

Metric (analytic) management for application instances: CRUD over the `Analytic` entities, and
translation of a metric into the query language of the target monitoring backend. A library jar — the
controller and service are picked up by the backend's component scan, so the endpoints are served on
the backend's port.

## Contents

- **`rest`** — `MetricController`, mapped under `/api/v1/metric`. Reads the caller's `auth_token`
  cookie and forwards it on the outgoing calls.
- **`service`** — `MetricService`, plus two strategy pairs selected by `project.name`:
  `MetricConverterFactory` (`RainbowMetricConverter` / `DefaultMetricConverter`) builds the query, and
  `MetricClientFactory` (`RainbowClient` / `DefaultClient`) applies or deletes it on the monitoring
  backend.
- **`dto`** / **`enums`** — request and response objects, and the `MetricEnum` query-syntax constants.

## Configuration

| Property                   | Environment variable        | Default                        | Description                                                          |
| -------------------------- | --------------------------- | ------------------------------ | -------------------------------------------------------------------- |
| `metric.rest.uri`          | `METRIC_REST_URI`           | `http://localhost:8080/api/v1` | Base URI of the orchestrator API this module calls back into.        |
| `metric.rainbow.token`     | `RAINBOW_METRICS_TOKEN`     | *(empty)*                      | Bearer token for the Rainbow metrics API. Required for that project. |
| `metric.rainbow.port`      | `RAINBOW_METRICS_PORT`      | `5000`                         | Port of the Rainbow metrics API on each node.                        |
| `metric.rainbow.list.port` | `RAINBOW_METRICS_LIST_PORT` | `50000`                        | Port of the Rainbow metric listing endpoint.                         |

The properties are declared in the backend's `application.yml` / `application-production.yml`, since
that is the application that runs this code.

## Build

```
mvn -pl metric install
```
