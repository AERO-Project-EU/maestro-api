## 1. Install prerequisites
```
sudo apt-get install -y git vim
```

## 2. Git Hook Installation

#### 2.1 Server Side
```
mkdir maestro.git
mkdir maestro
cd maestro.git
git init --bare
cd hooks/
vim post-receive
chmod +x post-receive
```

#### 2.2 Client side
```
git remote add maestro <user>@<server-ip>:/home/<user>/maestro.git
git push maestro framework
```

## 3 Install rest of the prerequisites
```
cd ~/maestro/framework/development/maestro/
./initEnvironmentScript.sh
./volumeInitScript.sh
```

## 4 Generate the respective UI
#####4.1 Copy `.env.example` to `.env` and configure it properly
#####4.2 Create a docker-compose.yaml file that will be the one will run on the server.
#####4.3 Create a docker-compose-build.yaml file that will be the one will be used in order to build the docker images. 
Have in mind to change the frontent build component image name
```
docker-compose -f docker-compose-build.yml build --parallel
docker-compose -f docker-compose-build.yml push
```

## 5 Install Maestro at the server
#####5.1 Login to the docker registry that you are using at the server
#####5.2 Pull the docker images that you need
```
docker-compose pull --parallel
```
#####5.3 Start the instantiation of the platform
```
docker-compose up -d
```
