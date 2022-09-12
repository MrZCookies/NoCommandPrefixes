package com.mrzcookie.nocommandprefixes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class NoCommandPrefixes extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("[NoCommandPrefixes] Enabled NoCommandPrefixes");
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskTimer(this, () -> {
            Bukkit.getLogger().info("[NoCommandPrefixes] Checking for Command Prefixes...");
            try {
                Object result = getPrivateField(Bukkit.getServer().getPluginManager(), "commandMap");
                SimpleCommandMap commandMap = (SimpleCommandMap) result;
                Object map = getPrivateField(commandMap, "knownCommands");
                HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
                Set<String> commandsRemoved = new HashSet<>();
                for (String command : knownCommands.keySet()) {
                    if (getConfig().getBoolean("Disable-All-Prefixes")) {
                        if (command.contains(":")) {
                            commandsRemoved.add(command);
                        }
                    }
                    for (String prefix : getConfig().getStringList("Prefixes")) {
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
        }, 20L * 5L, 20L * getConfig().getInt("Interval"));
    }


    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(command -> command.contains(":"));
    }

    private Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = field.equals("commandMap") ? clazz.getDeclaredField(field) : field.equals("knownCommands") ? clazz.getSuperclass().getDeclaredField(field) : clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        Object result = objectField.get(object);
        objectField.setAccessible(false);
        return result;
    }
}
