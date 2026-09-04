#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

echo $'\nInstalling Docker Engine\n'
# Docker no longer ships as `docker`/`docker-engine`; also drop the podman shims
# that RHEL/CentOS/Rocky/Alma preinstall and that conflict with docker-ce.
sudo dnf remove -y docker \
                   docker-client \
                   docker-client-latest \
                   docker-common \
                   docker-latest \
                   docker-latest-logrotate \
                   docker-logrotate \
                   docker-engine \
                   podman \
                   runc || true

sudo dnf -y install dnf-plugins-core
# dnf5 (Fedora 41+) renamed the subcommand; dnf4 (RHEL/CentOS/Rocky/Alma 8-10) keeps --add-repo.
if sudo dnf config-manager --help 2>&1 | grep -q -- '--add-repo'; then
  sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
else
  sudo dnf config-manager addrepo --from-repofile=https://download.docker.com/linux/centos/docker-ce.repo
fi

# Compose is now a CLI plugin (`docker compose`), not a standalone binary.
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo $'\nConfiguring docker daemon json for IPv6\n'
sudo mkdir -p /etc/docker
# ip6tables is required for IPv6 NAT/port publishing on Docker Engine 27+.
echo '{
    "ipv6": true,
    "fixed-cidr-v6": "2001:db8::/64",
    "ip6tables": true
}' | sudo tee /etc/docker/daemon.json

sudo systemctl enable --now docker
sudo systemctl restart docker

sudo groupadd -f docker
sudo usermod -aG docker "${USER}"

echo $'\nInstalling shell completion\n'
sudo dnf install -y bash-completion
# `docker compose` completion comes from the docker CLI itself.
docker completion bash | sudo tee /etc/bash_completion.d/docker > /dev/null

echo $'\nInstalling Netdata\n'
# kickstart-static64.sh is retired; kickstart.sh is the unified installer.
curl -fsSL https://get.netdata.cloud/kickstart.sh -o /tmp/netdata-kickstart.sh
sudo sh /tmp/netdata-kickstart.sh --stable-channel --static-only --dont-wait

# Static installs live under /opt/netdata; native packages under /etc + /usr/libexec.
if [ -d /opt/netdata/etc/netdata ]; then
  NETDATA_CONF_DIR="/opt/netdata/etc/netdata"
  NETDATA_PLUGIN_DIR="/opt/netdata/usr/libexec/netdata"
else
  NETDATA_CONF_DIR="/etc/netdata"
  NETDATA_PLUGIN_DIR="/usr/libexec/netdata"
fi

# NOTE: python.d.plugin is deprecated upstream (go.d is the supported path), but it
# still runs custom collectors such as the orchestrator one below.
NETDATA_SRC="${REPO_ROOT}/ext-deps/netdata/orchestratorMetric"
sudo install -m 0644 "${NETDATA_SRC}/netdata.conf"                  "${NETDATA_CONF_DIR}/"
sudo install -D -m 0644 "${NETDATA_SRC}/orchestratorCollector.conf" "${NETDATA_CONF_DIR}/python.d/orchestratorCollector.conf"
sudo install -D -m 0755 "${NETDATA_SRC}/orchestratorCollector.chart.py" "${NETDATA_PLUGIN_DIR}/python.d/orchestratorCollector.chart.py"

sudo systemctl restart netdata

echo $'\nDone. Log out and back in for the docker group membership to take effect.\n'
