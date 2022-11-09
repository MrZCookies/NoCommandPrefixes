package com.mrzcookie.nocommandprefixes;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public record ReloadFallbackRemoverCommand(FallbackRemoverPlugin plugin) implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reload();
        plugin.removeUnwantedCommands();
        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded fallback remover plugin!");
        return true;
    }
}
