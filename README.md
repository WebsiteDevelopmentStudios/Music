# Music

Paper 26.2 custom music plugin with Maven-generated Java and Bedrock resource packs.

## Build

Requires JDK 25, Maven, and Python 3:

```sh
mvn -U clean package
```

Build outputs in `target/`:
- `Music.jar` — Paper plugin
- `Music-Java-Resource-Pack.zip` — Java resource-pack archive
- `Music-Geyser.mcpack` — Bedrock resource-pack archive

To embed tracks in the build artifacts, put the `.ogg` files in the repository's `music/` folder first. You can instead point the generator at a server music folder.

Linux:
```sh
MUSIC_SOURCE_DIR="/path/to/server/plugins/Music/music" mvn clean package
```

Windows PowerShell:
```powershell
$env:MUSIC_SOURCE_DIR = "C:/path/to/server/plugins/Music/music"
mvn clean package
```

Filenames may contain only letters, numbers, underscores, and hyphens. If no OGG files are supplied during the build, valid pack archives are still generated without track audio.

## Install and import tracks

1. Put `Music.jar` in the Paper server's `plugins` folder and restart.
2. Put `.ogg` files in `plugins/Music/music/`.
3. Run `/music reload`. This rescans tracks and exports current packs to `plugins/Music/Music-Java-Resource-Pack.zip` and `plugins/Music/Music-Geyser.mcpack`.
4. Distribute the Java pack through your server's resource-pack setup. Import the MCPACK to test on Bedrock clients.

The plugin broadcasts music from the target player's location, so players nearby can hear it. Java sound definitions set a 10-block attenuation distance; the sound fades with distance and is intended to be inaudible at 10 blocks. Actual attenuation can vary by client/resource-pack version.

The plugin generates the pack files but does not host or automatically deliver them to clients. Geyser support for custom sound events depends on your Geyser version/configuration; test with an actual Bedrock client before relying on it.

## Commands

- `/music list`
- `/music play <track> [player]`
- `/music stop [player]`
- `/music reload`
- `/music admin`
- `/music disable <world>` (toggle a world on/off)

Configuration: `volume`, `sound-volume`, `banned-players`, and `disabled-worlds`. Permissions: `music.use`, `music.admin`.
