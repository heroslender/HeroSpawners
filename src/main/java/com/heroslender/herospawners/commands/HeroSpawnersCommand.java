package com.heroslender.herospawners.commands;

import com.heroslender.herospawners.HeroSpawners;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HeroSpawnersCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("herospawners.admin")) {
            sender.sendMessage(ChatColor.GREEN + "HeroSpawners v" + HeroSpawners.getInstance().getDescription().getVersion());
            return true;
        }

        HeroSpawners.getInstance().reload();

        sender.sendMessage(ChatColor.GREEN + "HeroSpawners - " + ChatColor.YELLOW + "Configuração recarregada!");
        return true;
    }
}
