package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.ConfigurationController;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Utilities;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;
import de.dustplanet.util.SilkUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Heroslender.
 */
public class SilkSpawnersListener implements Listener {
    @Getter(AccessLevel.PRIVATE) private final Logger logger = HeroSpawners.getInstance().getLogger();
    private final ConfigurationController config;
    private final StorageController storageController;
    private final SilkUtil su;

    public SilkSpawnersListener(ConfigurationController configurationController, StorageController storageController) {
        Bukkit.getLogger().info("[herospawners] SilkSpawners foi encontrado!");

        this.config = configurationController;
        this.storageController = storageController;
        su = SilkUtil.hookIntoSilkSpanwers();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e) {
        if (HeroSpawners.getInstance().isShutingDown()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
            return;
        }

        ISpawner spawner = storageController.getSpawner(e.getBlock().getLocation());
        if (spawner == null) {
            return;
        }

        if (!spawner.getOwner().equals(e.getPlayer().getName()) && !e.getPlayer().hasPermission("herospawners.break.others")) {
            e.getPlayer().sendMessage(ChatColor.RED + "Não tens permissão para quebrar os spawners de outros players!");
            e.setCancelled(true);
            return;
        }


        int amount = e.getPlayer().isSneaking() ? Math.min(spawner.getAmount(), 64) : 1;
        ItemStack spawnerItemStack = su.newSpawnerItem(e.getEntityID(), su.getCustomSpawnerName(e.getEntityID()), amount, false);

        if (spawner.getAmount() > amount) {
            e.setCancelled(true);
            e.getPlayer().getInventory().addItem(spawnerItemStack)
                    .values()
                    .forEach(itemStack ->
                            spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), itemStack)
                    );

            spawner.setAmount(spawner.getAmount() - amount);

            getLogger().log(
                    Level.FINEST,
                    "{0} broken {1} from {2}",
                    new Object[]{e.getPlayer().getName(), amount, spawner}
            );
        } else {
            e.setDrop(spawnerItemStack);

            storageController.deleteSpawner(spawner);

            getLogger().log(
                    Level.FINEST,
                    "{0} broken all {1} from {2}",
                    new Object[]{e.getPlayer().getName(), amount, spawner}
            );
        }
    }

    @EventHandler
    public void onSpawnerPlace(final SilkSpawnersSpawnerPlaceEvent e) {
        if (!e.isCancelled() && e.getBlock().getType() == HeroSpawners.SPAWNER_TYPE) {
            if (HeroSpawners.getInstance().isShutingDown()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNão é possivel colocar spawners quando o servidor esta a ligar/desligar.");
                return;
            }

            for (val block : Utilities.getBlocks(e.getBlock(), config.getStackRadious())) {
                if (block.getType() != HeroSpawners.SPAWNER_TYPE
                        || !Objects.equals(su.getSpawnerEntityID(block), e.getEntityID())) {
                    continue;
                }

                ISpawner spawner = storageController.getSpawner(block.getLocation());
                if (spawner == null) continue;

                if (spawner.getAmount() < config.getStackLimit() || config.getStackLimit() <= 0) {
                    int quantidade = 1;
                    val itemInHand = e.getPlayer().getItemInHand();

                    if (e.getPlayer().isSneaking() && itemInHand.getType() == HeroSpawners.SPAWNER_TYPE) {
                        // Colocar aos packs se tiver agachado
                        final String entityID = su.getStoredSpawnerItemEntityID(itemInHand);
                        if (Objects.equals(entityID, e.getEntityID())) {
                            if (config.getStackLimit() <= 0) {
                                quantidade = itemInHand.getAmount();
                            } else {
                                val maxQuantAllowed = config.getStackLimit() - spawner.getAmount();
                                quantidade = Math.min(maxQuantAllowed, itemInHand.getAmount());
                            }
                        }
                    }

                    e.setCancelled(true);

                    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        // Se não tiver no criativo, remover os spawners do inv
                        if (itemInHand.getAmount() <= quantidade) {
                            itemInHand.setType(Material.AIR);
                        } else {
                            itemInHand.setAmount(itemInHand.getAmount() - quantidade);
                        }

                        e.getPlayer().setItemInHand(itemInHand);
                    }

                    spawner.setAmount(spawner.getAmount() + quantidade);

                    getLogger().log(
                            Level.FINEST,
                            "{0} stacked +{1} on {2}",
                            new Object[]{e.getPlayer().getName(), quantidade, spawner}
                    );

                    try {
                        block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                    } catch (NoSuchFieldError error) {
                        // ignored, since 1.13
                    }
                    return;
                }
            }

            val spawner = new Spawner(e.getPlayer().getName(), e.getBlock().getLocation(), 1);
            storageController.saveSpawner(spawner);

            getLogger().log(
                    Level.FINEST,
                    "{0} created stack on {1}",
                    new Object[]{e.getPlayer().getName(), spawner}
            );
        }
    }
}
