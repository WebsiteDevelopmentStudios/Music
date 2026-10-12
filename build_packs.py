#!/usr/bin/env python3
"""Generate distinct Java and Bedrock resource packs from imported OGG tracks."""
import json
import shutil
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / "target"
JAVA = TARGET / "Music-Java-Resource-Pack"
BEDROCK = TARGET / "Music-Geyser-pack"
JAVA_ASSETS = JAVA / "assets" / "blemmsmusic"
BEDROCK_SOUNDS = BEDROCK / "sounds"
for path in (JAVA, BEDROCK):
    if path.exists():
        shutil.rmtree(path)
JAVA_ASSETS.mkdir(parents=True, exist_ok=True)
(BEDROCK_SOUNDS / "music").mkdir(parents=True, exist_ok=True)

# The first locations support local development; the plugin data folder is
# also checked when the project is built from a server root.
source_dirs = [
    ROOT / "music",
    ROOT / "plugins" / "Music" / "music",
    ROOT / "src" / "pack" / "assets" / "blemmsmusic" / "sounds" / "music",
]
tracks = {}
for folder in source_dirs:
    if not folder.is_dir():
        continue
    for file in sorted(folder.glob("*.ogg")):
        if file.stem and all(c.isalnum() or c in "_-" for c in file.stem):
            tracks.setdefault(file.stem.lower(), file)

java_sounds = {}
bedrock_sounds = {}
for track_id, source in sorted(tracks.items()):
    java_name = f"blemmsmusic:music/{track_id}"
    java_sounds[f"music.{track_id}"] = {
        "category": "record",
        "sounds": [{"name": java_name, "stream": True, "attenuation_distance": 10}]
    }
    bedrock_sounds[f"music.{track_id}"] = {
        "category": "record",
        "max_distance": 10,
        "sounds": [{"name": f"sounds/music/{track_id}", "stream": True}]
    }
    shutil.copy2(source, JAVA_ASSETS / "sounds" / "music" / f"{track_id}.ogg")
    shutil.copy2(source, BEDROCK_SOUNDS / "music" / f"{track_id}.ogg")

# Java pack: correct Java-only metadata and sound registry.
(JAVA / "pack.mcmeta").write_text(json.dumps({
    "pack": {"pack_format": 75, "description": "Music plugin tracks (10 block range)"}
}, indent=2) + "\\n", encoding="utf-8")
(JAVA_ASSETS / "sounds.json").write_text(json.dumps(java_sounds, indent=2) + "\\n", encoding="utf-8")

# Bedrock pack: separate manifest and Bedrock sound_definitions format.
(BEDROCK / "manifest.json").write_text(json.dumps({
    "format_version": 2,
    "header": {
        "name": "Music Plugin Tracks",
        "description": "Custom music tracks for Bedrock clients",
        "uuid": "d4cbeef0-3a20-4a12-96ab-6d2ef56f4d11",
        "version": [1, 0, 1],
        "min_engine_version": [1, 20, 0]
    },
    "modules": [{
        "type": "resources",
        "uuid": "8fcb22a4-82f7-4b3f-9c7b-bd251d5e24a1",
        "version": [1, 0, 1]
    }]
}, indent=2) + "\\n", encoding="utf-8")
(BEDROCK_SOUNDS / "sound_definitions.json").write_text(json.dumps({
    "format_version": "1.14.0",
    "sound_definitions": bedrock_sounds
}, indent=2) + "\\n", encoding="utf-8")

TARGET.mkdir(parents=True, exist_ok=True)
for folder, output in ((JAVA, TARGET / "Music-Java-Resource-Pack.zip"),
                       (BEDROCK, TARGET / "Music-Geyser.mcpack")):
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for file in sorted(folder.rglob("*")):
            if file.is_file():
                archive.write(file, file.relative_to(folder).as_posix())

print(f"Generated Java ZIP and Bedrock MCPACK with {len(tracks)} OGG track(s).")
if not tracks:
    print("No OGG files found. Put files in ./music/ (or plugins/Music/music/) before building to include them.")
