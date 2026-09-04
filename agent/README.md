# Agent

The agent runs on each VM, registers the component with Consul and drives the optional security
services (Wazuh, Auditbeat/Packetbeat, Snort, OwlH forensics, XDP).

It is started with four positional arguments:

```
java -jar agent.jar <graphHexId> <graphInstanceHexId> <componentNodeHexId> <componentNodeInstanceHexId>
```

## Configuration

Everything deployment specific is read from the environment (see `eu.orchestrator.agent.configuration.AgentConfiguration`).
Only `AGENT_ENCRYPTION_KEY` is mandatory, the rest fall back to the defaults listed below.

### Secrets (no defaults)

| Variable                        | Description                                                                                                                                                                      |
| ------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AGENT_ENCRYPTION_KEY`          | **Required.** Shared secret with the orchestrator, used to decrypt the credentials stored in the Consul KV (`Encryption`). The agent throws on encrypt/decrypt when it is unset. |
| `AGENT_FORENSIC_AGENT_USERNAME` | Repository user for downloading the OwlH client.                                                                                                                                 |
| `AGENT_FORENSIC_AGENT_PASSWORD` | Repository password for downloading the OwlH client.                                                                                                                             |

### Endpoints

| Variable                           | Default      | Description                                                                                                           |
| ---------------------------------- | ------------ | --------------------------------------------------------------------------------------------------------------------- |
| `AGENT_CONSUL_HOST`                | `localhost`  | Consul agent host.                                                                                                    |
| `AGENT_CONSUL_PORT`                | `8500`       | Consul agent port.                                                                                                    |
| `AGENT_NETDATA_PORT`               | `19999`      | Netdata port used for metrics scraping and health checks.                                                             |
| `AGENT_COMPONENT_METRIC_PORT`      | `15568`      | Port of the component metric endpoint registered in Consul.                                                           |
| `AGENT_METRIC_EXPORTER_PORT`       | `48013`      | Port the agent's own metric exporter binds to.                                                                        |
| `AGENT_METADATA_URL`               | _(empty)_    | Cloud metadata endpoint for the public IPv4.                                                                          |
| `AGENT_DOCKER_BRIDGE_IP`           | `172.17.0.1` | Docker bridge address, skipped during IP discovery.                                                                   |
| `AGENT_AUDIT_LOGS_KAFKA_BOOTSTRAP` | _(empty)_    | `host:port` of the Kafka broker the auditbeat output is pointed to. Set it before enabling the legacy auditbeat flow. |
| `AGENT_AUDIT_LOGS_KAFKA_TOPIC`     | `audit-logs` | Kafka topic for the audit logs.                                                                                       |
| `AGENT_FORENSIC_SERVER_HOST`       | _(empty)_    | OwlH server address written into `conf.json`. Installation is skipped when unset.                                     |
| `AGENT_FORENSIC_AGENT_URL`         | _(empty)_    | URL of the OwlH client installer.                                                                                     |

### Host layout

| Variable                       | Default                       | Description                                                                    |
| ------------------------------ | ----------------------------- | ------------------------------------------------------------------------------ |
| `AGENT_HOST_VOLUME_PATH`       | `/home/ubuntu/docker_volume/` | Root of the Docker volumes created on the host.                                |
| `AGENT_HOST_WORKING_DIRECTORY` | `/home/ubuntu`                | Host directory used by the disk benchmark, the audit rules and the XDP helper. |
| `AGENT_XDP_INTERFACE`          | `ens3`                        | Interface the XDP DDoS blacklist attaches to.                                  |
| `AGENT_SNORT_ALERT_FILE`       | `/opt/snort/logs/alert`       | Snort alert file consumed by the intrusion detection service.                  |

### Beats and timing

| Variable                                            | Default                                        | Description                                    |
| --------------------------------------------------- | ---------------------------------------------- | ---------------------------------------------- |
| `AGENT_BEATS_DOWNLOAD_BASE_URL`                     | `https://artifacts.elastic.co/downloads/beats` | Base URL for the Beats `.deb` packages.        |
| `AGENT_AUDITBEAT_VERSION`                           | `7.17.0`                                       | Auditbeat version.                             |
| `AGENT_AUDITBEAT_LEGACY_VERSION`                    | `7.11.1`                                       | Auditbeat version used by the legacy SOC flow. |
| `AGENT_PACKETBEAT_VERSION`                          | `7.17.0`                                       | Packetbeat version.                            |
| `AGENT_MAX_ITERATIONS`                              | `10`                                           | Retries per bootstrap step.                    |
| `AGENT_MIN_WAITING_TIME` / `AGENT_MAX_WAITING_TIME` | `1000` / `60000`                               | Backoff bounds in milliseconds.                |

The Wazuh manager IP and agent package URL are not environment variables: they come from the
`AgentParameters` entry in the Consul KV (`socManagerIp`, `socAgentUrl`).
