# MDM Offline

Kotlin Multiplatform desktop server for LAN-only Mobile Device Management.

## Features

- First-launch tutorial (English / Italian)
- Instant LAN setup: UDP discovery broadcast + persistent TCP
- SQLite central database (`Devices`, `Commands`, `Events`, `Configurations`)
- Live device list, events, and command dispatch

## Run

```bash
./gradlew :mdm_serverapp:run
```

Hot reload:

```bash
./gradlew :mdm_serverapp:hotRun --auto
```

## Protocol

- UDP broadcast on port `9877`: `SERVER_ONLINE|9876`
- TCP on port `9876`: newline-delimited JSON messages (`HANDSHAKE`, `EVENT`, `COMMAND`, …)

## Data location

Server database: `~/.mdm_offline/server.db`
