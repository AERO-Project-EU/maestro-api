FROM centos:7

RUN curl -L 'https://github.com/prometheus/prometheus/releases/download/v2.23.0/prometheus-2.23.0.linux-amd64.tar.gz' -o /tmp/prometheus.tar.gz && \
	mkdir /opt/prometheus && \
	tar xvfz /tmp/prometheus.tar.gz  -C /opt/prometheus/ --strip-components 1 && \
	rm /tmp/prometheus.tar.gz && \
	mkdir -p /prometheus/config

VOLUME ["/prometheus"]

CMD  /opt/prometheus/prometheus --config.file="/prometheus/config/prometheus.yml" --web.enable-lifecycle