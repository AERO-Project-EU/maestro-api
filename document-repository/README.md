# Document Repository

Spring Data MongoDB repositories for the orchestrator's document-stored entities. A plain library
jar, not a service: it has no `main`, no resources and no connection settings of its own.

## Usage

The consumer owns the Mongo configuration. In the backend, `MongoConfig` enables these repositories
when `mongo.enabled=true`:

```java
@EnableMongoRepositories(basePackages = "eu.orchestrator.document.repository.dao")
```

Connection settings come from the consumer's `spring.data.mongodb.*`. Mongo is opt-in in the backend
(`mongo` profile or `mongo.enabled=true`); when it is off, the auto-configuration is excluded and the
injections of this DAO are guarded, so those code paths simply skip Mongo persistence.

## Build

```
mvn -pl document-repository install
```
