package com.heroslender.herospawners.spawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.spawners.SpawnerItemFactory;
import com.heroslender.herospawners.utils.Utilities;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class SpawnerBlockListener implements Listener {
    private final ConfigurationController config;
    private final StorageController storageController;

    @EventHandler(ignoreCancelled = false)
    private void onSpawnerPlace(final BlockPlaceEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER
                || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        val itemInHand = e.getItemInHand();
        val itemEntityType = SpawnerItemFactory.getEntityType(itemInHand);
        if (itemEntityType == null) {
            // Tried to place a spawner with an invalid itemStack.
            e.getPlayer().sendMessage(ChatColor.RED + "Esse spawner não é válido! Avise um membro da staff.");
            e.setCancelled(true);
            return;
        }
        val itemAmount = SpawnerItemFactory.getItemStackAmount(itemInHand);

        for (val block : Utilities.getBlocks(e.getBlock(), config.getStackRadious())) {
            if (block.getType() != Material.MOB_SPAWNER) {
                continue;
            }

            val state = block.getState();
            if (!(state instanceof CreatureSpawner)) {
                e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#1");
                e.setCancelled(true);
                return;
            }

            if (((CreatureSpawner) state).getSpawnedType() != itemEntityType) {
                continue;
            }

            ISpawner spawner = storageController.getSpawner(block.getLocation());
            if (spawner == null) {
                continue;
            }

            if (spawner.getAmount() < config.getStackLimit() || config.getStackLimit() <= 0) {
                int amountToStack = 1;

                if (e.getPlayer().isSneaking()) {
                    // Place the whole stack if sneaking
                    amountToStack = config.hasStackLimit()
                            // Has no limit
                            ? itemAmount
                            // Use the config limit
                            : Math.min(config.getStackLimit() - spawner.getAmount(), itemAmount);
                }

                if (amountToStack <= 0) {
                    e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#2");
                    e.setCancelled(true);
                    return;
                }

                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (amountToStack != itemAmount) {
                        // placed only a part of the stack
                        ItemStack spawnerItemStack = SpawnerItemFactory.newItemStack(itemEntityType, itemAmount - amountToStack);
                        if (spawnerItemStack == null) {
                            e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#3");
                            e.setCancelled(true);
                            return;
                        }

                        e.getPlayer().getInventory().addItem(spawnerItemStack)
                                .values()
                                .forEach(itemStack ->
                                        spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), itemStack)
                                );
                    }

                    if (itemInHand.getAmount() == 1) {
                        itemInHand.setType(Material.AIR);
                    } else {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }

                    e.getPlayer().setItemInHand(itemInHand);
                }

                e.setCancelled(true);
                spawner.setAmount(spawner.getAmount() + amountToStack);
                block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                return;
            }
        }

        int amountToStack = 1;

        if (e.getPlayer().isSneaking()) {
            // Place the whole stack if sneaking
            amountToStack = config.hasStackLimit()
                    // Has no limit
                    ? itemAmount
                    // Use the config limit
                    : Math.min(config.getStackLimit(), itemAmount);
        }

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (amountToStack != itemAmount) {
                // placed only a part of the stack
                ItemStack spawnerItemStack = SpawnerItemFactory.newItemStack(itemEntityType, amountToStack);
                if (spawnerItemStack == null) {
                    e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#3");
                    e.setCancelled(true);
                    return;
                }

                e.getPlayer().getInventory().addItem(spawnerItemStack)
                        .values()
                        .forEach(itemStack ->
                                e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), itemStack)
                        );
            }

            if (itemInHand.getAmount() == 1) {
                itemInHand.setType(Material.AIR);
            } else {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            }

            e.getPlayer().setItemInHand(itemInHand);
        }

        val spawner = new Spawner(e.getPlayer().getName(), e.getBlock().getLocation(), amountToStack);
        storageController.saveSpawner(spawner);
    }

    @EventHandler(ignoreCancelled = false)
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER
                || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        val spawner = storageController.getSpawner(e.getBlock().getLocation());
        if (spawner == null) {
            return;
        }

        val silkCheck = checkSilktouck(e.getPlayer());
        if (!silkCheck && !config.isDestroySilktouch()) {
            // The player doesn't have silk-touch and it's configured not to destroy the spawner
            // So we do nothing here, and cancel the event
            e.setCancelled(true);
            return;
        }

        val entityType = SpawnerItemFactory.getEntityType(e.getBlock());
        if (entityType == null) {
            e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao quebrar o spawner! " + ChatColor.GRAY + "#1");
            e.setCancelled(true);
            return;
        }

        val amount = e.getPlayer().isSneaking() ? spawner.getAmount() : 1;

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE && silkCheck) {
            // Give the broken spawners to the player if he's not in CREATIVE and passed the silktouch check
            ItemStack spawnerItemStack = SpawnerItemFactory.newItemStack(entityType, amount);
            if (spawnerItemStack == null) {
                e.getPlayer().sendMessage(ChatColor.RED + "Ocurreu um erro ao quebrar o spawner! " + ChatColor.GRAY + "#2");
                e.setCancelled(true);
                return;
            }

            e.getPlayer().getInventory().addItem(spawnerItemStack)
                    .values()
                    .forEach(itemStack ->
                            spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), itemStack)
                    );
        }

        spawner.setAmount(spawner.getAmount() - amount);
        if (spawner.getAmount() > 0) {
            e.setCancelled(true);
        } else {
            System.out.println("Delete spawner");
            storageController.deleteSpawner(spawner);
        }
    }

    private boolean checkSilktouck(@NotNull final Player player) {
        if (!config.isRequireSilktouch()) {
            return true;
        }

        val itemInHand = player.getItemInHand();
        if (!itemInHand.hasItemMeta()) {
            return false;
        }

        val meta = itemInHand.getItemMeta();
        if (!meta.hasEnchants() || !meta.hasEnchant(Enchantment.SILK_TOUCH)) {
            return false;
        }

        return config.getSilktouchLevel() <= 1 || meta.getEnchantLevel(Enchantment.SILK_TOUCH) >= config.getSilktouchLevel();
    }
}
