# Transfer

Shared transfer objects exchanged between the modules external services, plus a few helpers. A library jar with no configuration of its own.

## Contents

- **`entities`** — one package per module (`agent`, `backend`, `orchestrator`, `kubernetes`, `soc`,
  `virtualizationManager`, …), holding the DTOs sent to and from it.
- **`response`** — the common REST envelope (`RestResponse`, `ResponseCode`, `GeneralMessages`).
- **`util`** — `Encryption` and `Util`: AES encrypt/decrypt (the key is always passed in by the
  caller), hashing, serialization and random identifier generation.

## Build

```
mvn -pl transfer install
```
