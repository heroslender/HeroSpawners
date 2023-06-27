package com.heroslender.herospawners.feature.spawner.silkspawners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.service.ConfigurationService;
import com.heroslender.herospawners.service.SpawnerService;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.utils.Utilities;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;
import de.dustplanet.util.SilkUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by Heroslender.
 */
public class SilkSpawnersListener implements Listener {
    @Getter(AccessLevel.PRIVATE) private final Logger logger = HeroSpawners.getInstance().getLogger();
    private final ConfigurationService config;
    private final SpawnerService spawnerService;
    private final SilkUtil su;

    public SilkSpawnersListener(ConfigurationService configurationService, SpawnerService spawnerService) {
        Bukkit.getLogger().info("[herospawners] SilkSpawners foi encontrado!");

        this.config = configurationService;
        this.spawnerService = spawnerService;
        su = SilkUtil.hookIntoSilkSpanwers();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent e) {
        if (HeroSpawners.getInstance().isShutingDown()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
            return;
        }

        ISpawner spawner = spawnerService.getSpawner(e.getBlock().getLocation());
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

        spawnerService.updateSpawner(e.getPlayer(), spawner, spawner.getAmount() - amount);

        if (spawner.getAmount() >= 1) {
            e.setCancelled(true);
            e.getPlayer().getInventory().addItem(spawnerItemStack)
                    .values()
                    .forEach(itemStack ->
                            spawner.getLocation().getWorld().dropItemNaturally(spawner.getLocation(), itemStack)
                    );
        } else {
            e.setDrop(spawnerItemStack);
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

                ISpawner spawner = spawnerService.getSpawner(block.getLocation());
                if (spawner == null) {
                    continue;
                }

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

                    spawnerService.updateSpawner(e.getPlayer(), spawner, spawner.getAmount() + quantidade);
                    return;
                }
            }

            spawnerService.saveSpawner(e.getPlayer(), ((CreatureSpawner) e.getBlock().getState()), 1);
        }
    }
}
