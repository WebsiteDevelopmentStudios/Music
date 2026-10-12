Resource-pack source folder. Put .ogg tracks in:
assets/blemmsmusic/sounds/music/
The Maven build packages this folder into the Java ZIP and Bedrock .mcpack ZIP. For each track, the sound identifier is blemmsmusic:music.<filename-without-extension>; a matching sounds.json entry is required. Java and Bedrock sound definitions differ, so validate/test each pack on its target client. The plugin's runtime music folder is plugins/Music/music/ and is not automatically available to Maven running on a separate build machine.
