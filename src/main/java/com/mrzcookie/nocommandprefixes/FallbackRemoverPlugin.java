package com.mrzcookie.nocommandprefixes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public final class FallbackRemoverPlugin extends JavaPlugin implements Listener {
    private Map<String, Command> cachedKnownCommands;
    private BukkitTask checkTask;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();

        if (cachedKnownCommands == null) {
            getLogger().severe("Failed to get known command list, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("reloadfallbackremover").setExecutor(new ReloadFallbackRemoverCommand(this));
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::removeUnwantedCommands, 20L);

        getLogger().info("Successfully enabled " + getDescription().getName() + " by " + String.join(", ", getDescription().getAuthors()));
    }

    public void reload() {
        reloadConfig();
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel();
            checkTask = null;
        }
        if (getConfig().getBoolean("Periodically-Check")) {
            long interval = 20 * getConfig().getLong("Interval");
            checkTask = Bukkit.getScheduler().runTaskTimer(this, this::removeUnwantedCommands, interval, interval);
        }
        updateKnownCommandsInstance();
    }

    public void removeUnwantedCommands() {
        if (cachedKnownCommands == null) return;
        Set<String> toRemove = new HashSet<>();
        boolean disableAllPrefixes = getConfig().getBoolean("Disable-All-Prefixes");
        commandLoop:
        for (String command : cachedKnownCommands.keySet()) {
            if (!command.contains(":")) continue;
            String plugin = command.split(":")[0];
            if (disableAllPrefixes) {
                for (String prefix : getConfig().getStringList("Prefixes")) if (plugin.contains(prefix)) continue commandLoop;
                toRemove.add(command);
            }
            else for (String prefix : getConfig().getStringList("Prefixes")) if (plugin.contains(prefix)) {
                toRemove.add(command);
                continue commandLoop;
            }
        }
        for (String cmd : toRemove) cachedKnownCommands.remove(cmd);
        if (!toRemove.isEmpty()) getLogger().info("Removed " + toRemove.size() + " fallback commands");
    }

    @SuppressWarnings("unchecked")
    public void updateKnownCommandsInstance() {
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);

            Map<String, Command> map = (Map<String, Command>) knownCommandsField.get(commandMapField.get(Bukkit.getPluginManager()));
            if (map == null) return;

            cachedKnownCommands = map;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            getLogger().severe("Failed to reflectively get known command map");
            e.printStackTrace();
        }
    }
}
