# Elasticity

SLO management for application instances: CRUD over the `Slo` entities, plus translation of an SLO
into a Kubernetes `ServiceGraph`. A library jar — the controller and service are
picked up by the backend's component scan, so the endpoints are served on the backend's port.

## Contents

- **`rest`** — `ElasticityQueriesController`, mapped under `/api/v1/elasticity`. Reads the caller's
  `auth_token` cookie and forwards it on the outgoing calls.
- **`service`** — `ElasticityQueriesService`. Persists SLOs and fetches/applies the service graph.
- **`dto`** / **`enums`** — request and response objects.

## Configuration

| Property              | Environment variable  | Default                        | Description                                                                         |
| --------------------- | --------------------- | ------------------------------ | ----------------------------------------------------------------------------------- |
| `elasticity.rest.uri` | `ELASTICITY_REST_URI` | `http://localhost:8080/api/v1` | Base URI of the orchestrator API this module calls back into for the service graph. |

The property is declared in the backend's `application.yml` / `application-production.yml`, since
that is the application that runs this code.

## Build

```
mvn -pl elasticity install
```
