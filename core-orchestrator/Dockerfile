FROM eclipse-temurin:17-jre-noble

COPY ./target/core-orchestrator-1.6.1.RELEASE.jar /app/
COPY ./src/main/resources/initUbuntuWorker_IPv6.sh /app/
COPY ./src/main/resources/initUbuntuWorker_IPv4.sh /app/

COPY ./src/main/resources/traefik_linux-amd64 /usr/sbin/
RUN chmod +x /usr/sbin/traefik_linux-amd64

RUN mkdir /app/data/
CMD cd /app && java -jar core-orchestrator-1.6.1.RELEASE.jar --spring.profiles.active="production"