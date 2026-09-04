### MAESTRO -PROJECT SPECIFIC- INSTALLATION GUIDE

This guide provides the basics steps needed to perform a custom installation of Μaestro tailored to a project's needs.

#### Fetch Repository

Clone the repository and create a new branch from master to start working with.

#### [Helper] Local setup

To set up maestro, initially:  

1. Delete ~/.m2/repository/eu folder if exists

2. Build project, under maestro main folder run:
```
mvn clean install
```

For the rest of the components:

```
1. DATABASE

cd framework/development/common
docker-compose up -d database
docker-compose down
docker volume prune
*docker system prune
telnet localhost 3306

2. BACKEND

cd backend
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000"


[Optional, but if needed]
4. ORCHESTRATOR
/core-orchestrator/core

mvn spring-boot:run

-------------------------------------
```

You are now ready to start development process.

#### Setting up core files for project specific installation

Under the folder framework/development/ you will find some existing installations of Maestro for other projetcs (Rainbow, Spider, Matilda etc.)

Now in a similar way create a new folder to host the core files for the new installation

```
cd framework/development/
mkdir new-project-installation #there is no name convention
```

The setup needs the following files:

- **.env** : Copy `.env.example` to `.env` (it is git-ignored) and fill it in. This file contains all of the project's environment variables to help maintain the project structure cleaner and easy to configure. For every new installation we must define the PROJECT_NAME, SERVER_IP and SERVER_BASE_PATH according to project's standards.

- **docker-compose.yml** : The services that comprise our application. A minimal installation consists of the ui-backend, phpmyadmin, database, and landoop. Choose to add more services if needed for the project

- **docker-compose-build.yml** : Build the images for the project specific needs. For a minimal installation only ui-backend is enough. Then log in to your registry, build and push the images using this snippet:

    ```
    docker login <your-registry>   # use your registry credentials
    docker-compose -f docker-compose-build.yml build
    docker-compose -f docker-compose-build.yml push
    ```
This script is intended to run locally.

- **initEnvironmentScript-debian.sh** or **initEnvironmentScript.sh** : This is a script intended to run on the host server to install the needed service such as docker, docker-compose, netdata etc. Depending on the OS (debian based, centos etc.) you will need to use the appropriate package managers and commands.

- **update.sh** : This is a server script that will roll out smoothly new changes in your app by terminating and removing running containers (frontend/backend) and pulling and instantiating the updated ones located in the registry. Be sure to push first your updated docker images.

- **volumeInitScript.sh** : A server script to initialize some paths and volumes for the services.

- **README.md** : A project specific guide with explicit steps or helpful commands.


Copy files to the remote server and start working:
```
scp -r framework/development/[project]/* framework/development/[project]/.env servername@serverIP:~/[project]/
```
