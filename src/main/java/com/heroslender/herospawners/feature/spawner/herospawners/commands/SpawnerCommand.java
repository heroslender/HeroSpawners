package com.heroslender.herospawners.feature.spawner.herospawners.commands;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.feature.spawner.herospawners.SpawnerItemFactory;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SpawnerCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("herospawners.admin")) {
            sender.sendMessage(ChatColor.GREEN + "HeroSpawners v" + HeroSpawners.getInstance().getDescription().getVersion());
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("§cO jogador §7" + args[0] + " §cnão se encontra online!");
            return true;
        }

        EntityType type = EntityType.PIG;
        if (args.length > 1) {
            try {
                type = EntityType.valueOf(args[1].replace('-', '_').toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "A entidade '§7" + args[1] + "§c' não existe.");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "A entidade não foi especificada, utilizando PIG como padrão.");
        }

        int stackSize = 1;
        if (args.length > 2) {
            try {
                stackSize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cO valor §7" + args[2] + " §cnão representa uma quantidade válida!");
            }
        }

        val item = SpawnerItemFactory.newItemStack(type, stackSize);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Ocurreu um erro ao inicializar o item.");
            return true;
        }

        int multiplier = 1;
        if (args.length > 3) {
            try {
                multiplier = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cO valor §7" + args[3] + " §cnão representa uma quantidade válida!");
            }
        }
        item.setAmount(multiplier);

        player.getInventory().addItem(item);
        sender.sendMessage("§aRecebeste um spawner de §7" + getNameCapitalized(type, ' ') + "§a!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("herospawners.admin")) {
            return Collections.emptyList();
        }

        val toComplete = args[args.length - 1].toUpperCase(Locale.ROOT);

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getName().toUpperCase(Locale.ROOT).startsWith(toComplete))
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.stream(EntityType.values())
                    .filter(entityType -> entityType.name().startsWith(toComplete))
                    .map(SpawnerCommand::getNameCapitalized)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
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
