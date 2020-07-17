package com.heroslender.herospawners.spawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.spawners.SpawnerItemFactory;
import com.heroslender.herospawners.utils.Utilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class SpawnerBlockListener implements Listener {
    @Getter(AccessLevel.PRIVATE) private final Logger logger = HeroSpawners.getInstance().getLogger();
    private final ConfigurationController config;
    private final StorageController storageController;

    private ISpawner getSpawnerIfMatch(@NotNull final Block block, @NotNull final EntityType entityType) {
        if (block.getType() != HeroSpawners.SPAWNER_TYPE) {
            return null;
        }

        val state = block.getState();
        if (!(state instanceof CreatureSpawner) || ((CreatureSpawner) state).getSpawnedType() != entityType) {
            return null;
        }

        return storageController.getSpawner(block.getLocation());
    }

    private int getAmountToStack(@NotNull final Player player, @Nullable final ISpawner spawner, final int amount) {
        if (!player.isSneaking()) {
            return 1;
        } else if (config.hasStackLimit()) {
            val spawnerAmount = spawner != null ? spawner.getAmount() : 0;
            return Math.min(config.getStackLimit() - spawnerAmount, amount);
        } else {
            return amount;
        }
    }

    private void giveItem(@NotNull final Player player, @NotNull final Location dropLocation, @NotNull final EntityType entityType, final int amount) {
        ItemStack spawnerItemStack = SpawnerItemFactory.newItemStack(entityType, amount);
        if (spawnerItemStack == null) {
            player.sendMessage(ChatColor.RED + "Ocurreu um erro ao colocar o spawner! " + ChatColor.GRAY + "#3");
            return;
        }

        val itemInHand = player.getItemInHand();
        if (itemInHand.getAmount() == 1) {
            player.setItemInHand(spawnerItemStack);
        } else {
            itemInHand.setAmount(itemInHand.getAmount() - 1);

            player.getInventory().addItem(spawnerItemStack)
                    .values()
                    .forEach(itemStack ->
                            dropLocation.getWorld().dropItemNaturally(dropLocation, itemStack)
                    );
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerPlace(final BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() != HeroSpawners.SPAWNER_TYPE
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
            ISpawner spawner = getSpawnerIfMatch(block, itemEntityType);
            if (spawner == null) {
                continue;
            }

            if (spawner.getAmount() < config.getStackLimit() || config.getStackLimit() <= 0) {
                val amountToStack = getAmountToStack(e.getPlayer(), spawner, itemAmount);
                if (amountToStack <= 0) {
                    continue;
                }

                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (amountToStack != itemAmount) {
                        // placed only a part of the stack
                        giveItem(e.getPlayer(), spawner.getLocation(), itemEntityType, itemAmount - amountToStack);
                    } else {
                        if (itemInHand.getAmount() == 1) {
                            itemInHand.setType(Material.AIR);
                        } else {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        }

                        e.getPlayer().setItemInHand(itemInHand);
                    }
                }

                e.setCancelled(true);
                spawner.setAmount(spawner.getAmount() + amountToStack);

                getLogger().log(
                        Level.FINEST,
                        "{0} stacked +{1} on {2}",
                        new Object[]{e.getPlayer().getName(), amountToStack, spawner}
                );

                try {
                    block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                } catch (NoSuchFieldError error) {
                    // ignored, since 1.13
                }
                return;
            }
        }

        val amountToStack = getAmountToStack(e.getPlayer(), null, itemAmount);

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE && amountToStack != itemAmount) {
            // placed only a part of the stack
            giveItem(e.getPlayer(), e.getBlock().getLocation(), itemEntityType, itemAmount - amountToStack);
        }

        val spawner = new Spawner(e.getPlayer().getName(), e.getBlock().getLocation(), amountToStack);
        storageController.saveSpawner(spawner);

        getLogger().log(
                Level.FINEST,
                "{0} created stack on {1}",
                new Object[]{e.getPlayer().getName(), spawner}
        );

        val state = e.getBlock().getState();
        if ((state instanceof CreatureSpawner)) {
            ((CreatureSpawner) state).setSpawnedType(itemEntityType);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onSpawnerBreak(final BlockBreakEvent e) {
        if (e.getBlock().getType() != HeroSpawners.SPAWNER_TYPE
                || HeroSpawners.getInstance().shutdownCheck(e, e.getPlayer())) {
            return;
        }

        val spawner = storageController.getSpawner(e.getBlock().getLocation());
        if (spawner == null) {
            return;
        }

        if (!spawner.getOwner().equals(e.getPlayer().getName()) && !e.getPlayer().hasPermission("herospawners.break.others")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Não tens permissão para quebrar os spawners de outros players!");
            e.setCancelled(true);
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

            getLogger().log(
                    Level.FINEST,
                    "{0} broken {1} from {2}",
                    new Object[]{e.getPlayer().getName(), amount, spawner}
            );
        } else {
            storageController.deleteSpawner(spawner);

            getLogger().log(
                    Level.FINEST,
                    "{0} broken all {1} from {2}",
                    new Object[]{e.getPlayer().getName(), amount, spawner}
            );
        }

        //noinspection IsCancelled
        if (config.isSpawnersDropXP() && e.isCancelled()) {
            e.getBlock().getWorld().spawn(e.getBlock().getLocation(), ExperienceOrb.class)
                    .setExperience(e.getExpToDrop());
        } else if (!config.isSpawnersDropXP()) {
            e.setExpToDrop(0);
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
        val silkLevel = meta.getEnchantLevel(Enchantment.SILK_TOUCH);
        if (!meta.hasEnchants() || silkLevel == 0) {
            return false;
        }

        return silkLevel >= config.getSilktouchLevel();
    }
}
