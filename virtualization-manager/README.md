# Virtualization-manager  
This module has the responsibility to interact with IAAS. It contains the following modules:

* communicator
> This module has the communication layer for virtualization-manager
> it has a rest endpoints API which allow to interact with other services.

* spi
> This module define the interface and all necessary objects interact with the IAAS .

* provider-adapter
> This module implements the interface. 

## Configuration

`vim-core/src/main/resources/application.yml` (development) ships empty; the production profile reads
both values from the environment.

| Property              | Environment variable   | Description                                                                                                       |
| --------------------- | ---------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `openstack.image.id`  | `IMAGE_ID`             | UUID of the golden image used when spawning instances.                                                            |
| `token.signer.secret` | `TOKEN_SIGNER_SECRET`  | **Secret.** Signs/encrypts tokens. Must match the backend, the core-orchestrator (`key.token`) and the agent (`AGENT_ENCRYPTION_KEY`). |

## Tests

The integration tests under `vim-core/src/test` talk to a real IAAS and take their credentials from
system properties, all defaulting to empty:

```
mvn test -Dopenstack.endpoint=<keystone-url> -Dopenstack.username=<user> -Dopenstack.password=<password> \
         -Daws.accessKeyId=<id> -Daws.secretKey=<key> \
         -Dgcp.clientEmail=<service-account> -Dgcp.privateKey=<pem> -Dgcp.project=<project>
```
