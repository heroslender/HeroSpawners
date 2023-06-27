package com.heroslender.herospawners.feature.spawner.herospawners;

import com.google.common.collect.Lists;
import com.heroslender.herospawners.HeroSpawners;
import com.heroslender.herospawners.service.ConfigurationService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpawnerItemFactory {

    private static ConfigurationService configurationService = null;

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
    public static ItemStack newItemStack(@NotNull final EntityType entityType, final int amount) {
        Objects.requireNonNull(entityType, "entityType is null");

        val itemStack = new ItemStack(HeroSpawners.SPAWNER_TYPE);
        val meta = itemStack.getItemMeta();
        if (!(meta instanceof BlockStateMeta)) {
            return null;
        }

        val blockState = ((BlockStateMeta) meta).getBlockState();
        if (!(blockState instanceof CreatureSpawner)) {
            return null;
        }

        ((CreatureSpawner) blockState).setSpawnedType(entityType);

        ((BlockStateMeta) meta).setBlockState(blockState);

        val itemProps = getConfig().getItemProperties();
        val entityProps = getConfig().getProperties(entityType);

        val placeholders = new String[]{ConfigurationService.TYPE_PLACEHOLDER, ConfigurationService.AMOUNT_PLACEHOLDER};
        val replaces = new String[]{entityProps.getDisplayName(), Integer.toString(amount)};
        val name = StringUtils.replaceEach(itemProps.getName(), placeholders, replaces);
        val lore = new ArrayList<String>();
        for (String loreLine : itemProps.getLore()) {
            lore.add(StringUtils.replaceEach(loreLine, placeholders, replaces));
        }

        meta.setDisplayName(name);
        meta.setLore(Lists.newArrayList(lore));
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static int getItemStackAmount(@NotNull final ItemStack itemStack) {
        val entityType = getEntityType(itemStack);
        if (entityType == null) {
            return 1;
        }

        return getItemStackAmount(itemStack, entityType);
    }

    public static int getItemStackAmount(@NotNull final ItemStack itemStack, @NotNull final EntityType entityType) {
        int amount = 1;
        if (!itemStack.hasItemMeta()) {
            return amount;
        }

        val meta = itemStack.getItemMeta();
        val itemProps = getConfig().getItemProperties();
        try {
            String line;
            if (itemProps.isAmountInName()) {
                if (!meta.hasDisplayName()) {
                    return amount;
                }

                line = meta.getDisplayName();
            } else {
                if (!meta.hasLore() || itemProps.getAmountLine() < 0 || meta.getLore().size() < itemProps.getAmountLine()) {
                    return amount;
                }

                line = meta.getLore().get(itemProps.getAmountLine());
            }

            final int amountEndlength;
            if (itemProps.isDeductEntityName()) {
                String defaultLine = itemProps.isAmountInName() ? itemProps.getName() : itemProps.getLore().get(itemProps.getAmountLine());
                val entityProps = getConfig().getProperties(entityType);
                defaultLine = StringUtils.replaceOnce(defaultLine, ConfigurationService.TYPE_PLACEHOLDER, entityProps.getDisplayName());

                amountEndlength = defaultLine.length() - (itemProps.getAmountBeginIndex() + ConfigurationService.AMOUNT_PLACEHOLDER.length());
            } else {
                amountEndlength = itemProps.getAmountEndlength();
            }

            amount = Integer.parseInt(line.substring(itemProps.getAmountBeginIndex(), line.length() - amountEndlength));
        } catch (NumberFormatException e) {
            // invalid number
        }

        return amount;
    }

    private static synchronized ConfigurationService getConfig() {
        if (configurationService == null) {
            configurationService = HeroSpawners.getInstance().getConfigurationController();
        }

        return configurationService;
    }
}
