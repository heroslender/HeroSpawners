package com.heroslender.herospawners.spawners.commands;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.spawners.SpawnerItemFactory;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SpawnerCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("herospawners.admin")) {
            sender.sendMessage(ChatColor.GREEN + "HeroSpawners v" + HeroSpawners.getInstance().getDescription().getVersion());
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players allowed!");
            return true;
        }

        EntityType type = EntityType.PIG;
        if (args.length > 0) {
            try {
                type = EntityType.valueOf(args[0].replace('-', '_').toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "A entidade '§7" + args[0] + "§c' não existe.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "A entidade não foi especificada, utilizando PIG como padrão.");
        }

        val item = SpawnerItemFactory.newItemStack(type);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Ocurreu um erro ao inicializar o item.");
            return true;
        }

        ((Player) sender).getInventory().addItem(item);
        sender.sendMessage("§aRecebeste um spawner de §7" + getNameCapitalized(type, ' ') + "§a!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        val toComplete = String.join(" ", args).toUpperCase(Locale.ROOT);

        return Arrays.stream(EntityType.values())
                .filter(entityType -> entityType.name().startsWith(toComplete))
                .map(SpawnerCommand::getNameCapitalized)
                .collect(Collectors.toList());
    }

    public static String getNameCapitalized(@NotNull final EntityType entityType) {
        return getNameCapitalized(entityType, '-');
    }

    public static String getNameCapitalized(@NotNull final EntityType entityType, final char join) {
        val buff = new StringBuilder();

        val parts = entityType.name().toLowerCase(Locale.ROOT).split("_");
        for (int i = 0; i < parts.length; i++) {
            val entityName = parts[i].toCharArray();
            entityName[0] = Character.toUpperCase(entityName[0]);
            buff.append(entityName);

            if (i != parts.length - 1) {
                buff.append(join);
            }
        }

        return buff.toString();
    }
}
