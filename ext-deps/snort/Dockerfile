FROM alpine:3.8

RUN apk update && \
  apk add --no-cache snort && \
  rm -f /tmp/* /etc/apk/cache/*

RUN mkdir -p /usr/lib/snort_dynamicpreprocessor && \
	mkdir -p /usr/lib/snort_dynamicrules

VOLUME /var/lib/snort/

CMD snort -A fast -q -c /var/lib/snort/etc/snort.conf -i any -N