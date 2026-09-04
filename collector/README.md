# Collector

Small in-process metric collector. `Collector` keeps the registered metrics and dimensions in memory
and `ReportingServer` exposes the latest values as a netdata-style JSON document over a plain socket.
The agent embeds it (`new Collector(port)`); `CollectorClient` is a sample client that reads one
report and prints it.

## Configuration

| Variable                | Default          | Description                                                                    |
| ----------------------- | ---------------- | ------------------------------------------------------------------------------ |
| `COLLECTOR_PORT`        | `9090`           | Port the reporting server binds to when no port is passed to the constructor.  |
| `COLLECTOR_CLIENT_HOST` | `localhost`      | Reporting server the sample client connects to.                                |
| `COLLECTOR_CLIENT_PORT` | `COLLECTOR_PORT` | Port the sample client connects to.                                            |

The client also accepts the host and port as positional arguments, which take precedence over the
environment:

```
java -cp collector.jar eu.orchestrator.collectorclient.CollectorClient [host] [port]
java -cp collector.jar eu.orchestrator.collectorclient.CollectorClient --help
```

With no arguments it reads from `$COLLECTOR_CLIENT_HOST:$COLLECTOR_CLIENT_PORT`, so it works
unchanged inside a container. It logs the endpoint it connects to before opening the socket.
