/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jspecify.annotations.Nullable;

public interface SpawnPlacementTypes {
    public static final SpawnPlacementType NO_RESTRICTIONS = ($$0, $$1, $$2) -> true;
    public static final SpawnPlacementType IN_WATER = ($$0, $$1, $$2) -> {
        if ($$2 == null || !$$0.getWorldBorder().isWithinBounds($$1)) {
            return false;
        }
        BlockPos $$3 = $$1.above();
        return $$0.getFluidState($$1).is(FluidTags.WATER) && !$$0.getBlockState($$3).isRedstoneConductor($$0, $$3);
    };
    public static final SpawnPlacementType IN_LAVA = ($$0, $$1, $$2) -> {
        if ($$2 == null || !$$0.getWorldBorder().isWithinBounds($$1)) {
            return false;
        }
        return $$0.getFluidState($$1).is(FluidTags.LAVA);
    };
    public static final SpawnPlacementType ON_GROUND = new SpawnPlacementType(){

        @Override
        public boolean isSpawnPositionOk(LevelReader $$0, BlockPos $$1, @Nullable EntityType<?> $$2) {
            if ($$2 == null || !$$0.getWorldBorder().isWithinBounds($$1)) {
                return false;
            }
            BlockPos $$3 = $$1.above();
            BlockPos $$4 = $$1.below();
            BlockState $$5 = $$0.getBlockState($$4);
            if (!$$5.isValidSpawn($$0, $$4, $$2)) {
                return false;
            }
            return this.isValidEmptySpawnBlock($$0, $$1, $$2) && this.isValidEmptySpawnBlock($$0, $$3, $$2);
        }

        private boolean isValidEmptySpawnBlock(LevelReader $$0, BlockPos $$1, EntityType<?> $$2) {
            BlockState $$3 = $$0.getBlockState($$1);
            return NaturalSpawner.isValidEmptySpawnBlock($$0, $$1, $$3, $$3.getFluidState(), $$2);
        }

        @Override
        public BlockPos adjustSpawnPosition(LevelReader $$0, BlockPos $$1) {
            BlockPos $$2 = $$1.below();
            if ($$0.getBlockState($$2).isPathfindable(PathComputationType.LAND)) {
                return $$2;
            }
            return $$1;
        }
    };
}

