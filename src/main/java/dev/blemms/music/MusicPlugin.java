package dev.blemms.music;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public final class MusicPlugin extends JavaPlugin implements TabExecutor {
    private final Map<String, String> tracks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static final String PREFIX = ChatColor.DARK_AQUA + "[Music] " + ChatColor.GRAY;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File folder = new File(getDataFolder(), "music");
        if (!folder.exists() && !folder.mkdirs()) getLogger().warning("Could not create music folder: " + folder);
        if (!new File(folder, "README.txt").exists()) {
            try (var out = new java.io.FileWriter(new File(folder, "README.txt"))) {
                out.write("Put .ogg audio files here, then run /music reload. Filenames become track IDs.\n" +
                    "An .obb is an Android expansion archive, not an audio format; extract usable .ogg tracks first.\n");
            } catch (Exception ex) { getLogger().warning("Could not write music folder README: " + ex.getMessage()); }
        }
        Objects.requireNonNull(getCommand("music")).setExecutor(this);
        Objects.requireNonNull(getCommand("music")).setTabCompleter(this);
        scanTracks();
        getLogger().info("Music enabled; found " + tracks.size() + " .ogg track(s).");
    }

    private void scanTracks() {
        tracks.clear();
        File folder = new File(getDataFolder(), "music");
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".ogg"));
        if (files == null) return;
        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4);
            if (id.matches("[A-Za-z0-9_-]+")) tracks.put(id, file.getName());
            else getLogger().warning("Skipping unsupported track filename (use letters, digits, _ or -): " + file.getName());
        }
    }

    private boolean isBanned(Player player) {
        List<String> banned = getConfig().getStringList("banned-players");
        return banned.stream().anyMatch(s -> s.equalsIgnoreCase(player.getName()) || s.equalsIgnoreCase(player.getUniqueId().toString()));
    }

    private boolean allowed(Player player) {
        if (isBanned(player)) { player.sendMessage(PREFIX + ChatColor.RED + "You are banned from using music commands."); return false; }
        if (!player.hasPermission("music.admin") && !player.hasPermission("music.use")) {
            player.sendMessage(PREFIX + ChatColor.RED + "You do not have permission."); return false;
        }
        if (getConfig().getStringList("disabled-worlds").stream().anyMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) {
            player.sendMessage(PREFIX + ChatColor.RED + "Music commands are disabled in this world."); return false;
        }
        return true;
    }

    private boolean admin(CommandSender sender) {
        if (sender.hasPermission("music.admin")) return true;
        sender.sendMessage(PREFIX + ChatColor.RED + "You need music.admin permission.");
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(PREFIX + "/music list | play <track> [player] | stop [player] | reload | admin | disable <world>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list" -> {
                if (sender instanceof Player p && !allowed(p)) return true;
                if (tracks.isEmpty()) sender.sendMessage(PREFIX + "No .ogg tracks found in plugins/Music/music/.");
                else sender.sendMessage(PREFIX + "Tracks (" + tracks.size() + "): " + String.join(", ", tracks.keySet()));
                return true;
            }
            case "play" -> {
                if (!(sender instanceof Player) && args.length < 3) {
                    sender.sendMessage(PREFIX + "Console usage: /music play <track> <player>"); return true;
                }
                Player target;
                String track;
                if (args.length >= 3 && admin(sender)) {
                    track = args[1];
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) { sender.sendMessage(PREFIX + "Player not found."); return true; }
                    if (isBanned(target)) { sender.sendMessage(PREFIX + "That player is banned from music commands."); return true; }
                } else {
                    if (!(sender instanceof Player p)) return true;
                    if (!allowed(p)) return true;
                    if (args.length < 2) { p.sendMessage(PREFIX + "Usage: /music play <track>"); return true; }
                    track = args[1]; target = p;
                }
                String canonical = tracks.keySet().stream().filter(k -> k.equalsIgnoreCase(track)).findFirst().orElse(null);
                if (canonical == null) { sender.sendMessage(PREFIX + "Unknown track. Use /music list."); return true; }
                String sound = "blemmsmusic:music." + canonical.toLowerCase(Locale.ROOT);
                float volume = (float) Math.max(0.0, Math.min(16.0,
                    getConfig().getDouble("volume", 1.0) * getConfig().getDouble("sound-volume", 1.0)));
                target.getWorld().playSound(target.getLocation(), sound, SoundCategory.RECORDS, volume, 1.0f);
                sender.sendMessage(PREFIX + "Playing " + ChatColor.AQUA + canonical + ChatColor.GRAY + " for " + target.getName() + ".");
                return true;
            }
            case "stop" -> {
                Player target;
                if (args.length >= 2 && admin(sender)) {
                    target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) { sender.sendMessage(PREFIX + "Player not found."); return true; }
                } else if (sender instanceof Player p) {
                    if (!allowed(p)) return true; target = p;
                } else { sender.sendMessage(PREFIX + "Console usage: /music stop <player>"); return true; }
                for (Player nearby : target.getWorld().getPlayers()) {\n                    if (nearby.getLocation().distanceSquared(target.getLocation()) <= 100.0) {\n                        for (String name : tracks.keySet()) nearby.stopSound("blemmsmusic:music." + name.toLowerCase(Locale.ROOT), SoundCategory.RECORDS);\n                    }\n                }
                target.sendMessage(PREFIX + "Stopped custom music.");
                if (sender != target) sender.sendMessage(PREFIX + "Stopped music for " + target.getName() + ".");
                return true;
            }
            case "reload" -> {
                if (!admin(sender)) return true;
                reloadConfig(); scanTracks();
                sender.sendMessage(PREFIX + "Reloaded config and found " + tracks.size() + " track(s).");
                return true;
            }
            case "admin" -> {
                if (!admin(sender)) return true;
                sender.sendMessage(ChatColor.AQUA + "Music admin");
                sender.sendMessage(ChatColor.GRAY + "/music reload - reload config and scan tracks");
                sender.sendMessage(ChatColor.GRAY + "/music disable <world> - toggle music commands in a world");
                sender.sendMessage(ChatColor.GRAY + "/music play <track> <player> - play a track for a player");
                sender.sendMessage(ChatColor.GRAY + "/music stop <player> - stop custom music for a player");
                sender.sendMessage(ChatColor.GRAY + "Track count: " + tracks.size());
                return true;
            }
            case "disable" -> {
                if (!admin(sender)) return true;
                if (args.length < 2) { sender.sendMessage(PREFIX + "Usage: /music disable <world>"); return true; }
                String requested = args[1];
                World world = Bukkit.getWorld(requested);
                String worldName = world == null ? requested : world.getName();
                List<String> disabled = new ArrayList<>(getConfig().getStringList("disabled-worlds"));
                Optional<String> existing = disabled.stream().filter(w -> w.equalsIgnoreCase(worldName)).findFirst();
                if (existing.isPresent()) {
                    disabled.remove(existing.get());
                    sender.sendMessage(PREFIX + "Music commands enabled in world " + worldName + ".");
                } else {
                    disabled.add(worldName);
                    sender.sendMessage(PREFIX + "Music commands disabled in world " + worldName + ".");
                }
                getConfig().set("disabled-worlds", disabled);
                saveConfig();
                return true;
            }
            default -> { sender.sendMessage(PREFIX + "Unknown subcommand. Use /music for help."); return true; }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return filter(List.of("list", "play", "stop", "reload", "admin", "disable"), args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("play")) return filter(new ArrayList<>(tracks.keySet()), args[1]);
        if (args.length == 2 && args[0].equalsIgnoreCase("disable")) return filter(Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("play") && sender.hasPermission("music.admin")) return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[2]);
        if (args.length == 2 && args[0].equalsIgnoreCase("stop") && sender.hasPermission("music.admin")) return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String prefix) {
        return values.stream().filter(v -> v.regionMatches(true, 0, prefix, 0, prefix.length())).collect(Collectors.toList());
    }
}
