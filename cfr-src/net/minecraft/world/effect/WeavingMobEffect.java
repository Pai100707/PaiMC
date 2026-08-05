/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.effect;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;

class WeavingMobEffect
extends MobEffect {
    private final ToIntFunction<RandomSource> maxCobwebs;

    protected WeavingMobEffect(MobEffectCategory $$0, int $$1, ToIntFunction<RandomSource> $$2) {
        super($$0, $$1, ParticleTypes.ITEM_COBWEB);
        this.maxCobwebs = $$2;
    }

    @Override
    public void onMobRemoved(ServerLevel $$0, LivingEntity $$1, int $$2, Entity.RemovalReason $$3) {
        if ($$3 == Entity.RemovalReason.KILLED && ($$1 instanceof Player || $$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue())) {
            this.spawnCobwebsRandomlyAround($$0, $$1.getRandom(), $$1.blockPosition());
        }
    }

    private void spawnCobwebsRandomlyAround(ServerLevel $$0, RandomSource $$1, BlockPos $$2) {
        HashSet $$3 = Sets.newHashSet();
        int $$4 = this.maxCobwebs.applyAsInt($$1);
        for (BlockPos $$5 : BlockPos.randomInCube($$1, 15, $$2, 1)) {
            BlockPos $$6 = $$5.below();
            if ($$3.contains($$5) || !$$0.getBlockState($$5).canBeReplaced() || !$$0.getBlockState($$6).isFaceSturdy($$0, $$6, Direction.UP)) continue;
            $$3.add($$5.immutable());
            if ($$3.size() < $$4) continue;
            break;
        }
        for (BlockPos $$7 : $$3) {
            $$0.setBlock($$7, Blocks.COBWEB.defaultBlockState(), 3);
            $$0.levelEvent(3018, $$7, 0);
        }
    }
}

