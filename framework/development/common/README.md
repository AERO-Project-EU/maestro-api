## 1 Install the prerequisites

```
cp ./* <your-target-folder>
./initEnvironmentScript.sh
./volumeInitScript.sh
```

## 2 Build maestro backend

```
docker compose build -f docker-compose-build.yml
```

## 3 Start the instantiation of the platform

```
docker compose up -d
```
