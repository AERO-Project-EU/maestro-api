## Maestro Framework Grafana

These are the Grafana pre-defined configuration files.

Grafana reads its data from Prometheus on **localhost:9000**, so the container must run with
`network_mode: host`.

Use the following commands, or run `../framework/setupEnvironment.sh`, to initialize Grafana:

```
sudo cp -r ../ext-deps/grafana/ /data/maestro/grafana
sudo chmod -R 777 /data/maestro/grafana/
```

### Credentials

No credentials are stored here. `data/grafana.db` ships with Grafana's own default admin account
(`admin` / `admin`). Set a real password on first login, or override it before the first start with:

```
GF_SECURITY_ADMIN_USER=<user>
GF_SECURITY_ADMIN_PASSWORD=<password>
```

Create API keys from the Grafana UI (Configuration → API keys) on your own instance. Do not commit
them here.
