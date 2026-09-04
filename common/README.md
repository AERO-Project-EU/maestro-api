# Common

Shared utilities used across the orchestrator modules.

The module has no configuration: no hosts, ports, paths or credentials. Its only dependency is
`jakarta.servlet-api`, declared `provided` because `LocaleUtil` reads the locale cookie off an
`HttpServletRequest`.

## Contents

- **`enums`** — `GenericMessage` (message catalogue: English text, Greek text and a numeric code per
  constant), `LocaleEnum` (`EN`, `EL`).
- **`exception`** — `GenericBusinessException`, `BadRequestBusinessException`, `NotFoundException`
  and `NotAuthorizedException`. Unchecked, each carrying a `GenericMessage` so the handler can render
  the localized text and code.
- **`util`** — `NullCheckUtil` (null-safe `isEmpty` / `isNotEmpty`) and `LocaleUtil` (reads the
  `CookieLocaleResolver` cookie).

```java
String message = GenericMessage.NOT_FOUND.getMessage(request);   // or getMessage(LocaleEnum.EL)
throw new NotFoundException("No provider with id " + id, GenericMessage.NOT_FOUND);
```

## Build

```
mvn -pl common install
```
