package com.heroslender.herospawners.listeners;

import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.controllers.StorageController;
import com.heroslender.herospawners.models.ISpawner;
import com.heroslender.herospawners.models.Spawner;
import com.heroslender.herospawners.utils.Config;
import com.heroslender.herospawners.utils.Utilities;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerPlaceEvent;
import de.dustplanet.util.SilkUtil;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Heroslender.
 */
public class SilkSpawnersListener implements Listener {
    private final StorageController storageController;
    private final SilkUtil su;

    public SilkSpawnersListener(StorageController storageController) {
        Bukkit.getLogger().info("[herospawners] SilkSpawners foi encontrado!");

        this.storageController = storageController;
        su = SilkUtil.hookIntoSilkSpanwers();
    }

    @EventHandler
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent event) {
        if (event.isCancelled()) return;

        ISpawner spawner = storageController.getSpawner(event.getBlock().getLocation());
        if (spawner == null)
            return;
        if (HeroSpawners.getInstance().isShutingDown()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
            return;
        }

        int quantidade = 1;
        if (event.getPlayer().isSneaking()) {
            quantidade = (spawner.getAmount() > 64 ? 64 : spawner.getAmount());
            event.setDrop(su.newSpawnerItem(event.getEntityID(), su.getCustomSpawnerName(su.eid2MobID.get(event.getEntityID())), quantidade, false));
        }

        if (spawner.getAmount() > quantidade) {
            final EntityType et = event.getSpawner().getSpawnedType();

            int finalQuantidade = quantidade;
            Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                event.getBlock().setType(Material.MOB_SPAWNER);
                CreatureSpawner creatureSpawner = ((CreatureSpawner) event.getBlock().getState());
                creatureSpawner.setSpawnedType(et);
                creatureSpawner.setDelay(200);
                creatureSpawner.update();

                spawner.setAmount(spawner.getAmount() - finalQuantidade);
                HeroSpawners.getInstance().newSpawner.add(event.getBlock().getLocation());
            }, 1L);
        } else {
            storageController.deleteSpawner(spawner);
        }
    }

    @EventHandler
    public void onSpawnerPlace(final SilkSpawnersSpawnerPlaceEvent e) {
        if (!e.isCancelled() && e.getBlock().getType() == Material.MOB_SPAWNER) {
            if (HeroSpawners.getInstance().isShutingDown()) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cNão é possivel colocar spawners quando o servidor esta a ligar/desligar.");
                return;
            }

            for (val block : Utilities.getBlocks(e.getBlock(), Config.JUNTAR_RAIO)) {
                if (block.getType() != Material.MOB_SPAWNER) continue;

                val cs = (CreatureSpawner) block.getState();
                if (cs.getSpawnedType().getTypeId() != e.getEntityID()) continue;


                ISpawner spawner = storageController.getSpawner(block.getLocation());
                if (spawner == null) continue;

                if (spawner.getAmount() < Config.JUNTAR_MAX || Config.JUNTAR_MAX <= 0) {
                    int quantidade = 1;
                    val itemInHand = e.getPlayer().getItemInHand();

                    if (e.getPlayer().isSneaking() && itemInHand.getType() == Material.MOB_SPAWNER) {
                        // Colocar aos packs se tiver agachado
                        short entityID = su.getStoredSpawnerItemEntityID(itemInHand);
                        if (entityID == 0 || !this.su.knownEids.contains(entityID)) {
                            entityID = this.su.getDefaultEntityID();
                        }

                        if (e.getEntityID() == entityID) {
                            if (Config.JUNTAR_MAX <= 0) {
                                quantidade = itemInHand.getAmount();
                            } else {
                                val maxQuantAllowed = Config.JUNTAR_MAX - spawner.getAmount();
                                quantidade = maxQuantAllowed > itemInHand.getAmount() ? itemInHand.getAmount() : maxQuantAllowed;
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
                    block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                    return;
                }
            }

            val spawner = new Spawner(e.getPlayer().getName(), e.getBlock().getLocation(), 1);
            storageController.saveSpawner(spawner);
        }
    }
}