package com.mrzcookie.nocommandprefixes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class NoCommandPrefixes extends JavaPlugin implements Listener {
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("[NoCommandPrefixes] Enabled NoCommandPrefixes");

        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(this, this);

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimer(this, () -> {
            Bukkit.getLogger().info("[NoCommandPrefixes] Checking for Command Prefixes...");

            try {
                Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                constructor.setAccessible(true);

                Field commandMap = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMap.setAccessible(true);
                SimpleCommandMap map = (SimpleCommandMap) commandMap.get(Bukkit.getPluginManager());

                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(map);

                Set<String> commandsRemoved = new HashSet<>();
                for (String command : knownCommands.keySet()) {
                    if (config.getBoolean("Disable-All-Prefixes")) {
                        if (command.contains(":")) {
                            commandsRemoved.add(command);
                        }
                    }

                    for (String prefix : config.getStringList("Prefixes")) {
                        if (command.contains(prefix + ":")) {
                            commandsRemoved.add(command);
                        }
                    }
                }

                Bukkit.getLogger().info("[NoCommandPrefixes] Removing Command Prefixes...");
                for (String command : commandsRemoved) {
                    knownCommands.remove(command);
                }

                commandsRemoved.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 20L * 5L, 20L * config.getInt("Interval"));
    }


    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Set<String> commandsRemoved = new HashSet<>();

        for (String command : event.getCommands()) {
            if (config.getBoolean("Disable-All-Prefixes")) {
                if (command.contains(":")) {
                    commandsRemoved.add(command);
                }
            }

            for (String prefix : config.getStringList("Prefixes")) {
                if (command.contains(prefix + ":")) {
                    commandsRemoved.add(command);
                }
            }
        }

        event.getCommands().removeAll(commandsRemoved);
    }
}
