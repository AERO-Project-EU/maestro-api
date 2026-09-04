# Metrics Exporter

This service extracts metrics from Prometheus for each component. It connects with the consul and retrieves all
healthy node from netdata service. After that,it makes a rest call to get the metrics from Prometheus and create proper object.

### Prerequisites

- consul 1.0.7
- Prometheus 2.2.1

### Current monitoring metrics streams

| metric Name     | metric unit | metric unit                                                     |
| --------------- | ----------- | --------------------------------------------------------------- |
| CPU utilization | %           | \_cpu_cpu_percentage_average{dimension='idle'}                  |
| Free Disk       | GB          | \_disk_space_GB_average{dimension='avail'}                      |
| Used Memory     | MB          | \_system_ram_MB_average{chart='system.ram',dimension='used'}    |
| Buffered Memory | MB          | \_system_ram_MB_average{chart='system.ram',dimension='buffers'} |
| Free Memory     | MB          | \_system_ram_MB_average{chart='system.ram',dimension='free'}    |
| Cached Memory   | MB          | \_system_ram_MB_average{chart='system.ram',dimension='cached'}  |

### Configuration

`application.yml` (development) defaults to a local stack, while `application-production.yml` reads
everything from the environment.

| Property                                  | Environment variable                 | Default              | Description                                   |
| ----------------------------------------- | ------------------------------------ | -------------------- | --------------------------------------------- |
| `system.prometheus.url` / `.port`         | `PROMETHEUS_URL` / `PROMETHEUS_PORT` | `localhost` / `9090` | Prometheus the metrics are queried from.      |
| `system.consul.url` / `.port`             | `CONSUL_URL` / `CONSUL_PORT`         | `localhost` / `8500` | Consul used to discover healthy nodes.        |
| `system.backend.host` / `.port`           | `BACKEND_URL` / `BACKEND_PORT`       | `localhost` / `8080` | Orchestrator backend.                         |
| `system.ports.netdata`                    | `NETDATA_PORT`                       | `19999`              | Netdata port the queries are scoped to.       |
| `system.ports.traefik`                    | `TRAEFIK_PORT`                       | `15568`              | Traefik port the proxy queries are scoped to. |
| `system.enablePolicyMonitoring`           | `ENABLED_MONITORING`                 | `true`               | Whether policy monitoring runs.               |
| `system.monitoringMetricEachMilliseconds` | `MONITORING_INTERVAL_MS`             | `10000`              | Polling interval in milliseconds.             |

### Start the service:

1. As stand-alone application

```
mvn clean install
cd metrics-extractor
mvn clean install
mvn spring-boot:run
```

2. As container

```
cd /framework/{proper_folder}
docker-compose up --build metric-exporter
```
