# Music

Paper 26.2 custom music plugin with resource-pack export scaffolding for Java and Bedrock/Geyser.

## Build

Requires JDK 25 and Maven:

```sh
mvn -U clean package
```

Artifacts are created in `target/`:
- `Music.jar` — Paper plugin
- `Music-Java-Resource-Pack.zip` — Java resource-pack archive
- `Music-Geyser.mcpack` — Bedrock resource-pack archive (a ZIP with a .mcpack extension)

## Install and add tracks

1. Put `Music.jar` in the Paper server's `plugins` folder and restart.
2. Put supported `.ogg` audio files in `plugins/Music/music/`. A starter folder is generated automatically.
3. Run `/music reload` to rescan tracks.
4. The plugin uses Minecraft custom sounds. For clients to hear custom tracks, the generated resource pack must contain matching converted audio assets and sound definitions, then be hosted and configured for the server/client. Geyser Bedrock packs require audio converted to a Bedrock-supported format and Bedrock-specific sound definitions; a simple renamed ZIP alone does not convert Java OGG audio.

## Commands

- `/music list`
- `/music play <track> [player]`
- `/music stop [player]`
- `/music reload`
- `/music admin`
- `/music disable <world>` (toggle a world on/off)

Permission nodes: `music.use`, `music.admin`.

## OBB note

An Android `.obb` is an expansion archive, not an audio codec. Extract legally usable audio from the archive and import supported audio files; this plugin intentionally does not decrypt or unpack proprietary game data.
