# maestro-backend

This maven module exposes a REST API to cover UI requirements.
It is based on the HATEOAS principles. Use **HAL Browser** (http://localhost:8080/api/v2) to navigate through the API. 

**DOCKER-library needs the following docker volume:** `/var/run/docker.sock:/var/run/docker.sock`    

### Running

#### Overriding application configuration for your dev. environment
We provide a `.env.example` file which can be used as a base to create a `.env` file which contains the configuration properties you want to override.
This allows changing the application configuration without persisting any changes to `application.yml`.

The keys must follow the naming convention defined in [binding from environment variables
guide of SpringBoot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables).
Thus, a config property `kafka.server.url` can be overridden by an environment variable named `KAFKA_SERVER_URL`.

To **load your .env** when you run the application you need to set the `env` of the command.

```bash
$ source .env
$ env $(cat .env | xargs) mvn spring-boot:run
```

#### Secrets

`application.yml` ships with every credential empty. At minimum set `TOKEN_SIGNER_SECRET` (shared
with the virtualization-manager, the core-orchestrator and the agent) and `DATABASE_PASSWORD`.
The Kubernetes providers seeded at startup (`initialization.provider.*`) read their cluster
credentials from `DATACLOUD_K8S_*` / `RAINBOW_K8S_*`; leave them empty to seed a provider with no
credentials rather than committing a cluster's admin key.

#### Enable hot-swapping of classes

---
**NOTE**

Limitation: Regardless of how you run the application (either within IntelliJ or via terminal), to trigger hot-swapping, **we need to use the
build module via IntelliJ**.
(Should be fixable with a bit more Google-fu.)

---

Via SpringBoot devtools dependency, we can use hot-swapping of classes without doing a cold-start of our application.

To enable automatic hot-swapping, open the hotSwap configuration menu:
Either search (Ctrl+Shift+A) for "HotSwap" or go to `Build, Execution, Deployment > Debugger > HotSwap`.

Ensure the settings are configured as shown below:

| Setting | Value |
|:---:|:---:|
| Build project before reloading classes | ✓ |
| Enable 'JVM will hang' warning |  |
| Reload classes in the background | ✓ |
| Reload classes after recompilation | Always |

Now, you can **manually** trigger a recompilation of the `backend` module via `Build > Build Module 'backend'`.
This will trigger the hot-swapping process and will automatically reload the new code into the JVM without issuing a restart on our application.

More in the related documentation, [here](https://docs.spring.io/spring-boot/docs/2.0.1.RELEASE/reference/html/using-boot-devtools.html).
