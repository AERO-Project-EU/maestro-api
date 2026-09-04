# Policy-engine
This service is the core policy engine of all the maestro. It is based on drools, 
which is a rule based policy engine. It combine the Production memory (rules) and 
the Working memory(Facts) to create a decision. 

####Lifecycle :
1. Read from Kafka topic `policy-expression` and retrieve **tranfer/policyModel**
2. Create prometheus rule file (yml), update prometheus configuration and restcall to reloaded it   
3. Create drools rule file (drl) and initial drool KieSession (production memory)
4. Prometheus make a POST rest call into policy-engine endpoint `/api/v1`. This endpoint create a 
fact drool rule and insert it into drool KieSession (working memory). After the fire the KieSession
5. If a drool rule trigger, the policy engine create a proposal object **tranfer/orchestratorProposalModel**
and push it into kafka topic `graph-triggered-actions`

#### Configuration

`application.yml` (development) points at a local Kafka/Prometheus; `application-production.yml`
reads the endpoints from the environment.

| Property                            | Environment variable        | Default                        | Description                                   |
| ----------------------------------- | --------------------------- | ------------------------------ | --------------------------------------------- |
| `system.prometheus.url` / `.port`   | `PROMETHEUS_URL` / `PROMETHEUS_PORT` | *(required in production)* | Prometheus whose config is rewritten and reloaded. |
| `system.prometheus.filePath`        | `PROMETHEUS_RULES_PATH`     | `/prometheus/config/`          | Where the generated alert rule files are written. |
| `system.kafka.url` / `.port`        | `KAFKA_URL` / `KAFKA_PORT`  | *(required in production)*     | Kafka broker.                                 |
| `system.kafka.orchestratorTopicName`| `KAFKA_ORCHESTRATOR_TOPIC`  | `graph-triggered-actions`      | Topic the proposals are pushed to.            |
| `system.kafka.backendTopicName`     | `KAFKA_BACKEND_TOPIC`       | `policy-expression-cruder`     | Topic the policy expressions are read from.   |
| `system.kafka.infoTopicName`        | `KAFKA_INFO_TOPIC`          | `graph-triggered-info`         | Topic the informational events are pushed to. |
| `system.drools.filePath`            | `DROOLS_RULES_PATH`         | `/rules/`                      | Where the generated `.drl` rule files are written. |

####Start the service:

> if you want to run kafka on localhost, on `policy-engine` module run `docker-compose up`



   1. Build project
 ```
cd maestro
mvn clean install
 ``` 
 
   2. Execute on development mode
 ```
cd  maestro/policy-engine
mvn spring-boot:run
 ``` 
 
   3. Execute on production mode
  ```
 cd  maestro/policy-engine
 mvn spring-boot:run -Drun.profiles=production
  ```