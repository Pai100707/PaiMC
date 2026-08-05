/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
    public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> $$0, EntitySpawnReason $$1, ServerLevel $$2, BlockPos $$3, int $$4, int $$5, int $$6, Strategy $$7, boolean $$8) {
        BlockPos.MutableBlockPos $$9 = $$3.mutable();
        for (int $$10 = 0; $$10 < $$4; ++$$10) {
            Mob $$13;
            int $$11 = Mth.randomBetweenInclusive($$2.random, -$$5, $$5);
            int $$12 = Mth.randomBetweenInclusive($$2.random, -$$5, $$5);
            $$9.setWithOffset($$3, $$11, $$6, $$12);
            if (!$$2.getWorldBorder().isWithinBounds($$9) || !SpawnUtil.moveToPossibleSpawnPosition($$2, $$6, $$9, $$7) || $$8 && !$$2.noCollision($$0.getSpawnAABB((double)$$9.getX() + 0.5, $$9.getY(), (double)$$9.getZ() + 0.5)) || ($$13 = (Mob)$$0.create($$2, null, $$9, $$1, false, false)) == null) continue;
            if ($$13.checkSpawnRules($$2, $$1) && $$13.checkSpawnObstruction($$2)) {
                $$2.addFreshEntityWithPassengers($$13);
                $$13.playAmbientSound();
                return Optional.of($$13);
            }
            $$13.discard();
        }
        return Optional.empty();
    }

    private static boolean moveToPossibleSpawnPosition(ServerLevel $$0, int $$1, BlockPos.MutableBlockPos $$2, Strategy $$3) {
        BlockPos.MutableBlockPos $$4 = new BlockPos.MutableBlockPos().set($$2);
        BlockState $$5 = $$0.getBlockState($$4);
        for (int $$6 = $$1; $$6 >= -$$1; --$$6) {
            $$2.move(Direction.DOWN);
            $$4.setWithOffset((Vec3i)$$2, Direction.UP);
            BlockState $$7 = $$0.getBlockState($$2);
            if ($$3.canSpawnOn($$0, $$2, $$7, $$4, $$5)) {
                $$2.move(Direction.UP);
                return true;
            }
            $$5 = $$7;
        }
        return false;
    }

    public static interface Strategy {
        @Deprecated
        public static final Strategy LEGACY_IRON_GOLEM = ($$0, $$1, $$2, $$3, $$4) -> {
            if ($$2.is(Blocks.COBWEB) || $$2.is(Blocks.CACTUS) || $$2.is(Blocks.GLASS_PANE) || $$2.getBlock() instanceof StainedGlassPaneBlock || $$2.getBlock() instanceof StainedGlassBlock || $$2.getBlock() instanceof LeavesBlock || $$2.is(Blocks.CONDUIT) || $$2.is(Blocks.ICE) || $$2.is(Blocks.TNT) || $$2.is(Blocks.GLOWSTONE) || $$2.is(Blocks.BEACON) || $$2.is(Blocks.SEA_LANTERN) || $$2.is(Blocks.FROSTED_ICE) || $$2.is(Blocks.TINTED_GLASS) || $$2.is(Blocks.GLASS)) {
                return false;
            }
            return !(!$$4.isAir() && !$$4.liquid() || !$$2.isSolid() && !$$2.is(Blocks.POWDER_SNOW));
        };
        public static final Strategy ON_TOP_OF_COLLIDER = ($$0, $$1, $$2, $$3, $$4) -> $$4.getCollisionShape($$0, $$3).isEmpty() && Block.isFaceFull($$2.getCollisionShape($$0, $$1), Direction.UP);
        public static final Strategy ON_TOP_OF_COLLIDER_NO_LEAVES = ($$0, $$1, $$2, $$3, $$4) -> $$4.getCollisionShape($$0, $$3).isEmpty() && !$$2.is(BlockTags.LEAVES) && Block.isFaceFull($$2.getCollisionShape($$0, $$1), Direction.UP);

        public boolean canSpawnOn(ServerLevel var1, BlockPos var2, BlockState var3, BlockPos var4, BlockState var5);
    }
}

