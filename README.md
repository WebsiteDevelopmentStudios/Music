# Music

Paper 26.2 music plugin with Maven-generated Java and Bedrock resource packs.

## Build

Requires JDK 25, Maven, and Python 3:

```sh
mvn -U clean package
```

Build outputs in `target/`:
- `Music.jar` — Paper plugin
- `Music-Java-Resource-Pack.zip` — Java resource pack
- `Music-Geyser.mcpack` — Bedrock resource pack archive

### Include your OGG tracks in the packs

Before building, put the `.ogg` files in the repository's `music/` folder. Alternatively, point the generator at the server plugin's music folder:

Linux:
```sh
MUSIC_SOURCE_DIR="/path/to/server/plugins/Music/music" mvn clean package
```

Windows PowerShell:
```powershell
$env:MUSIC_SOURCE_DIR = "C:\\path\\to\\server\\plugins\\Music\\music"
mvn clean package
```

The build generates a sound event and copies each supported OGG into both pack layouts. Track filenames should use only letters, numbers, underscores, and hyphens. If you do not provide OGG files, valid pack archives are still produced but contain no tracks.

## Install

1. Put `Music.jar` in the Paper server's `plugins` folder and restart.
2. Import audio as `.ogg` files into `plugins/Music/music/`.
3. Run `/music reload` to rescan tracks.
4. Build packs with those same OGG files, then distribute the Java ZIP through your server resource-pack setup. Import the MCPACK for Bedrock testing.

The plugin does not automatically upload resource packs or install them on clients. Geyser's handling of custom Java sound events depends on your Geyser setup and version; test the generated Bedrock pack with actual Bedrock clients.

## Playback and commands

- `/music list`
- `/music play <track> [player]`
- `/music stop [player]`
- `/music reload`
- `/music admin`
- `/music disable <world>` (toggle music commands in a world)

Music is played at the target player's location to nearby players. The generated Java sound definitions set a 10-block attenuation distance; the sound fades with distance and is intended to be inaudible at 10 blocks. Actual attenuation can vary with the client and resource-pack format. `volume` and `sound-volume` in config.yml control the playback volume.

Permissions: `music.use`, `music.admin`.
