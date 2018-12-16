package com.heroslender.HeroSpawners.events;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.ISpawner;
import com.heroslender.HeroSpawners.Spawner.Spawner;
import com.heroslender.HeroSpawners.Utils.Config;
import com.heroslender.HeroSpawners.Utils.Utilities;
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
    private final SilkUtil su;

    public SilkSpawnersListener() {
        Bukkit.getLogger().info("[HeroSpawners] SilkSpawners foi encontrado!");
        su = SilkUtil.hookIntoSilkSpanwers();
    }

    @EventHandler
    public void onSpawnerBreak(SilkSpawnersSpawnerBreakEvent event) {
        if (event.isCancelled()) return;

        ISpawner spawner = HeroSpawners.getInstance().getStorage().getSpawner(event.getBlock().getLocation());
        if (spawner == null)
            return;
        if (HeroSpawners.getInstance().isShutingDown()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cNão é possivel quebrar spawners quando o servidor esta a ligar/desligar.");
            return;
        }

        int quantidade = 1;
        if (event.getPlayer().isSneaking()) {
            quantidade = (spawner.getQuatidade() > 64 ? 64 : spawner.getQuatidade());
            event.setDrop(su.newSpawnerItem(event.getEntityID(), su.getCustomSpawnerName(su.eid2MobID.get(event.getEntityID())), quantidade, false));
        }

        if (spawner.getQuatidade() > quantidade) {
            final EntityType et = event.getSpawner().getSpawnedType();

            int finalQuantidade = quantidade;
            Bukkit.getScheduler().runTaskLater(HeroSpawners.getInstance(), () -> {
                event.getBlock().setType(Material.MOB_SPAWNER);
                CreatureSpawner creatureSpawner = ((CreatureSpawner) event.getBlock().getState());
                creatureSpawner.setSpawnedType(et);
                creatureSpawner.setDelay(200);
                creatureSpawner.update();

                spawner.setQuatidade(spawner.getQuatidade() - finalQuantidade);
                HeroSpawners.getInstance().newSpawner.add(event.getBlock().getLocation());
            }, 1L);
        } else {
            spawner.destroy();
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


                ISpawner spawner = HeroSpawners.getInstance().getStorage().getSpawner(block.getLocation());
                if (spawner == null) continue;

                if (spawner.getQuatidade() < Config.JUNTAR_MAX || Config.JUNTAR_MAX <= 0) {
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
                                val maxQuantAllowed = Config.JUNTAR_MAX - spawner.getQuatidade();
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

                    spawner.setQuatidade(spawner.getQuatidade() + quantidade);
                    block.getWorld().spigot().playEffect(block.getLocation(), Effect.WITCH_MAGIC, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 200, 10);
                    return;
                }
            }

            val spawner = new Spawner(e.getBlock().getLocation(), 1);
            spawner.save();
        }
    }
}
