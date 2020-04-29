package com.heroslender.herospawners.spawners;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SpawnerItemFactory {

    /**
     * Get the entityType attached to the given ItemStack.
     *
     * @param itemStack The itemStack to get the entity type from.
     * @return The {@link EntityType} if found, or {@code null} otherwise.
     */
    @Nullable
    public static EntityType getEntityType(@NotNull final ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack is null");

        if (!itemStack.hasItemMeta()) {
            return null;
        }

        val meta = itemStack.getItemMeta();
        if (!(meta instanceof BlockStateMeta)) {
            return null;
        }

        if (!((BlockStateMeta) meta).hasBlockState()) {
            return null;
        }

        val blockState = ((BlockStateMeta) meta).getBlockState();
        if (!(blockState instanceof CreatureSpawner)) {
            return null;
        }

        return getEntityType((CreatureSpawner) blockState);
    }

    /**
     * Get the entityType attached to the given Block.
     *
     * @param block The block to get the entity type from.
     * @return The {@link EntityType} if found, or {@code null} otherwise.
     */
    @Nullable
    public static EntityType getEntityType(@NotNull final Block block) {
        Objects.requireNonNull(block, "block is null");

        val state = block.getState();
        if (!(state instanceof CreatureSpawner)) {
            return null;
        }

        return getEntityType((CreatureSpawner) state);
    }

    /**
     * Get the entityType attached to the given CreatureSpawner.
     *
     * @param creatureSpawner The creatureSpawner {@link org.bukkit.block.BlockState} to get the entity type from.
     * @return The {@link EntityType}.
     */
    public static EntityType getEntityType(@NotNull final CreatureSpawner creatureSpawner) {
        Objects.requireNonNull(creatureSpawner, "creatureSpawner is null");

        return creatureSpawner.getSpawnedType();
    }

    @Nullable
    public static ItemStack newItemStack(@NotNull final EntityType entityType) {
        Objects.requireNonNull(entityType, "entityType is null");

        val itemStack = new ItemStack(Material.MOB_SPAWNER);
        val meta = itemStack.getItemMeta();
        if (!(meta instanceof BlockStateMeta)) {
            Bukkit.broadcastMessage("Meta not BlockStateMeta, is " + meta.getClass().getName());
            return null;
        }

        val blockState = ((BlockStateMeta) meta).getBlockState();
        if (!(blockState instanceof CreatureSpawner)) {
            Bukkit.broadcastMessage("State not CreatureSpawner, is " + blockState.getClass().getName());
            return null;
        }

        ((CreatureSpawner) blockState).setSpawnedType(entityType);

        ((BlockStateMeta) meta).setBlockState(blockState);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
