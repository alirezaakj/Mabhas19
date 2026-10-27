# Core modules

Present: `model` (IDs, edition, engineering number parsing), `domain` (repository contracts, clock/id injection), `calculation` (unit conversion only), `network` (A01 public adapter), `database` (Room entities, DAO, exported schema), `data` (local repositories, Hilt module, DataStore), `designsystem` (Persian RTL theme, numeric field) and `testing` (fakes, main-dispatcher rule).

`model`/`calculation` stay free of Android SDK imports; `testing` must not enter production artifacts. Module ownership and dependency direction are defined in [architecture](../../docs/android/architecture.md).
