<div align="center">

```
        __  ______   _________________  ____
        /  |/  / _ | / __/ __/_  __/ _ \/ __ \
       / /|_/ / __ |/ _/_\ \  / / / , _/ /_/ /
     /_/  /_/_/ |_/___/___/ /_/ /_/|_|\____/
```

**A novel reconfigurable by design highly distributed applications procurement paradigm over programmable infrastructure**

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE.md)
[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.x-6DB33F.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36.svg?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-27.x-2496ED.svg?logo=docker&logoColor=white)](https://www.docker.com/)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/)

</div>

## Prerequisites

- JDK 17.0_latest
- Maven 3.9.x
- Docker 27.x
- Docker Compose 5.x

Before moving on, make sure you have the required JDK and Maven version.

    $ mvn -version
    $ java -version
    $ javac -version
    $ docker --version
    $ docker-compose --version

## Change permissions

```
Grafana folder AND subfolders used as volume needs to change its permision from 715 to 717 before you spawn the framework

sudo mkdir -p /data/maestro/grafana/data
sudo mkdir -p /data/maestro/grafana/logs
sudo chmod 717 /data/maestro/grafana/data
sudo chmod 717 /data/maestro/grafana/logs

```

## Build the microservices and run the project

1. Move to project home folder

```
cd maestro (project home foler)
```

2. Build project microservices

```
mvn clean install
```

3. Build containerized services

```
cd framework/development/
docker-compose -f docker-compose-build.yml build
```

4. In main folder, create an .env file as in the .env.example and set variables accordingly

```
mv .env.example .env
vi .env
```

5. Run containerized services

```
docker compose up -d
```

The main docker compose contains the minimal ui, backend, database, phpmyadmin setup.

When the docker services are ready, you can use the frontend ui under http://serverIP:3000 where serverIP
is the one you set in the .env

## Run in development mode

Run only the infrastructure services in Docker and the backend locally, so you can rebuild and debug it
without rebuilding its image.

1. Start the database and phpmyadmin

```
docker compose up -d database phpmyadmin
```

2. Run the backend from the project home folder

```
mvn -pl backend -am spring-boot:run
```

The backend starts on `BACKEND_PORT` (8080 by default) and connects to the database exposed on
`DATABASE_PORT`. phpmyadmin is available under http://localhost:8010 (or the `PHPMYADMIN_PORT` you set).

Alternatively, run the backend as a container as well (after building its image as in the steps above):

```
docker compose up -d database phpmyadmin ui-backend
```

To rebuild and restart just the backend container after a code change:

```
mvn clean install
cd framework/development/common/
docker-compose -f docker-compose-build.yml build ui-backend
cd ../../..
docker compose up -d --force-recreate ui-backend
```

Follow the backend logs with:

```
docker compose logs -f ui-backend
```

## License

Copyright 2017-2026 Ubitech ltd (www.ubitech.eu).

Licensed under the Apache License, Version 2.0. See [LICENSE.md](LICENSE.md) for the full text,
or obtain a copy at http://www.apache.org/licenses/LICENSE-2.0.

## Acknowledgement

This work has been funded by the European Union under Horizon Europe grant 101092850
(project AERO).
