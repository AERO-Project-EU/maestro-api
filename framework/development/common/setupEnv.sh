sudo mkdir -p /data/maestro/prometheus/config
sudo cp ./maestro/ext-deps/prometheus/configs/prometheus.yml /data/maestro/prometheus/config
sudo mkdir -p /data/maestro/grafana/data
sudo mkdir -p /data/maestro/grafana/logs
sudo chmod 717 /data/maestro/grafana/data
sudo chmod 717 /data/maestro/grafana/logs