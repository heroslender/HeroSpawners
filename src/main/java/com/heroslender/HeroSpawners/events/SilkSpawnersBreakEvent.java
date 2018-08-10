package com.heroslender.HeroSpawners.events;

import com.heroslender.HeroSpawners.HeroSpawners;
import com.heroslender.HeroSpawners.Spawner.ISpawner;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerBreakEvent;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by Heroslender.
 */
public class SilkSpawnersBreakEvent implements Listener {

    private SilkUtil su;
//    HeroSpawners plugin;

    public SilkSpawnersBreakEvent() {
        Bukkit.getLogger().info("[HeroSpawners] SilkSpawners foi encontrado!");
        su = SilkUtil.hookIntoSilkSpanwers();
//        plugin = HeroSpawners.getInstance();
//        Bukkit.getPluginManager().registerEvents(this, HeroSpawners.getInstance());
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
}
