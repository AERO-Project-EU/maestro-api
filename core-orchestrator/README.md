# Orchestrator

This service has 2 active profiles:

##### 1. Development

This profile is for development environment. To execute the service enter on `core-orchestrator`
folder and run :

```
mvn spring-boot:run
```

or

```
java -jar core-orchestor-1.6.1.RELEASE.jar
```

##### 2. Production

This profile is for production environment. To execute the service enter on `core-orchestrator`
folder and run :

```
mvn spring-boot:run -Drun.profiles=production
```

or

```
java -jar core-orchestor-1.6.1.RELEASE.jar --spring.profiles.active="production"
```

### Configuration

`application.yml` (development) ships with empty values; `application-production.yml` reads
everything from the environment. Properties that affect how the worker VMs are provisioned:

| Property                    | Environment variable        | Description                                                                                             |
| --------------------------- | --------------------------- | ------------------------------------------------------------------------------------------------------- |
| `key.token`                 | `KEY_TOKEN`                 | Shared secret with the agent. Must match the agent's `AGENT_ENCRYPTION_KEY`.                            |
| `worker.username`           | `WORKER_USERNAME`           | Default user of the worker image (default `ubuntu`).                                                    |
| `worker.password`           | `WORKER_PASSWORD`           | Password set for that user during provisioning. **Leave empty** to keep the image's own credentials.    |
| `network.additional.routes` | `NETWORK_ADDITIONAL_ROUTES` | Comma separated CIDRs routed via the private gateway on each worker. Empty means no extra routes.       |
| `cjdns.init.script.url`     | `CJDNS_INIT_SCRIPT_URL`     | URL of the cjdns init script, installed as `/etc/init.d/cjdns`. Host it yourself; empty skips the step. |

### Comments

Execute docker run command for traefik

```
docker run -d -p 8080:8080 -p 80:80 traefik -c --api --consul.prefix=DBMS/852d1786/dbManagerLB/lbConfiguration --consul.endpoint=<consul-host>:8500
```

```
kafka-topics  --create --zookeeper <zookeeper-host>:2181 --replication-factor 1 --partitions 1 --topic backend-requests
kafka-topics  --create --zookeeper <zookeeper-host>:2181 --replication-factor 1 --partitions 1 --topic graph-triggered-actions
```
