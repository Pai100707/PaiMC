/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface RandomizableContainer
extends Container {
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";

    public @Nullable ResourceKey<LootTable> getLootTable();

    public void setLootTable(@Nullable ResourceKey<LootTable> var1);

    default public void setLootTable(ResourceKey<LootTable> $$0, long $$1) {
        this.setLootTable($$0);
        this.setLootTableSeed($$1);
    }

    public long getLootTableSeed();

    public void setLootTableSeed(long var1);

    public BlockPos getBlockPos();

    public @Nullable Level getLevel();

    public static void setBlockEntityLootTable(BlockGetter $$0, RandomSource $$1, BlockPos $$2, ResourceKey<LootTable> $$3) {
        BlockEntity $$4 = $$0.getBlockEntity($$2);
        if ($$4 instanceof RandomizableContainer) {
            RandomizableContainer $$5 = (RandomizableContainer)((Object)$$4);
            $$5.setLootTable($$3, $$1.nextLong());
        }
    }

    default public boolean tryLoadLootTable(ValueInput $$0) {
        ResourceKey $$1 = $$0.read(LOOT_TABLE_TAG, LootTable.KEY_CODEC).orElse(null);
        this.setLootTable($$1);
        this.setLootTableSeed($$0.getLongOr(LOOT_TABLE_SEED_TAG, 0L));
        return $$1 != null;
    }

    default public boolean trySaveLootTable(ValueOutput $$0) {
        ResourceKey<LootTable> $$1 = this.getLootTable();
        if ($$1 == null) {
            return false;
        }
        $$0.store(LOOT_TABLE_TAG, LootTable.KEY_CODEC, $$1);
        long $$2 = this.getLootTableSeed();
        if ($$2 != 0L) {
            $$0.putLong(LOOT_TABLE_SEED_TAG, $$2);
        }
        return true;
    }

    default public void unpackLootTable(@Nullable Player $$0) {
        Level $$1 = this.getLevel();
        BlockPos $$2 = this.getBlockPos();
        ResourceKey<LootTable> $$3 = this.getLootTable();
        if ($$3 != null && $$1 != null && $$1.getServer() != null) {
            LootTable $$4 = $$1.getServer().reloadableRegistries().getLootTable($$3);
            if ($$0 instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)$$0, $$3);
            }
            this.setLootTable(null);
            LootParams.Builder $$5 = new LootParams.Builder((ServerLevel)$$1).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$2));
            if ($$0 != null) {
                $$5.withLuck($$0.getLuck()).withParameter(LootContextParams.THIS_ENTITY, $$0);
            }
            $$4.fill(this, $$5.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }
    }
}

