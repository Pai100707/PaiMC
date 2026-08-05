/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BrushItem
extends Item {
    public static final int ANIMATION_DURATION = 10;
    private static final int USE_DURATION = 200;

    public BrushItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        Player $$1 = $$0.getPlayer();
        if ($$1 != null && this.calculateHitResult($$1).getType() == HitResult.Type.BLOCK) {
            $$1.startUsingItem($$0.getHand());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack $$0) {
        return ItemUseAnimation.BRUSH;
    }

    @Override
    public int getUseDuration(ItemStack $$0, LivingEntity $$1) {
        return 200;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void onUseTick(Level $$0, LivingEntity $$1, ItemStack $$2, int $$3) {
        boolean $$10;
        void $$5;
        block11: {
            block10: {
                if ($$3 < 0 || !($$1 instanceof Player)) {
                    $$1.releaseUsingItem();
                    return;
                }
                Player $$4 = (Player)$$1;
                HitResult $$6 = this.calculateHitResult((Player)$$5);
                if (!($$6 instanceof BlockHitResult)) break block10;
                BlockHitResult $$7 = (BlockHitResult)$$6;
                if ($$6.getType() == HitResult.Type.BLOCK) break block11;
            }
            $$1.releaseUsingItem();
            return;
        }
        int $$9 = this.getUseDuration($$2, $$1) - $$3 + 1;
        boolean bl = $$10 = $$9 % 10 == 5;
        if ($$10) {
            SoundEvent $$16;
            Block block;
            HumanoidArm $$13;
            void $$8;
            BlockPos $$11 = $$8.getBlockPos();
            BlockState $$12 = $$0.getBlockState($$11);
            HumanoidArm humanoidArm = $$13 = $$1.getUsedItemHand() == InteractionHand.MAIN_HAND ? $$5.getMainArm() : $$5.getMainArm().getOpposite();
            if ($$12.shouldSpawnTerrainParticles() && $$12.getRenderShape() != RenderShape.INVISIBLE) {
                this.spawnDustParticles($$0, (BlockHitResult)$$8, $$12, $$1.getViewVector(0.0f), $$13);
            }
            if ((block = $$12.getBlock()) instanceof BrushableBlock) {
                BrushableBlock $$14 = (BrushableBlock)block;
                SoundEvent $$15 = $$14.getBrushSound();
            } else {
                $$16 = SoundEvents.BRUSH_GENERIC;
            }
            $$0.playSound((Entity)$$5, $$11, $$16, SoundSource.BLOCKS);
            if ($$0 instanceof ServerLevel) {
                BrushableBlockEntity $$18;
                boolean $$19;
                ServerLevel $$17 = (ServerLevel)$$0;
                BlockEntity blockEntity = $$0.getBlockEntity($$11);
                if (blockEntity instanceof BrushableBlockEntity && ($$19 = ($$18 = (BrushableBlockEntity)blockEntity).brush($$0.getGameTime(), $$17, (LivingEntity)$$5, $$8.getDirection(), $$2))) {
                    EquipmentSlot $$20 = $$2.equals($$5.getItemBySlot(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                    $$2.hurtAndBreak(1, (LivingEntity)$$5, $$20);
                }
            }
        }
    }

    private HitResult calculateHitResult(Player $$0) {
        return ProjectileUtil.getHitResultOnViewVector($$0, EntitySelector.CAN_BE_PICKED, $$0.blockInteractionRange());
    }

    private void spawnDustParticles(Level $$0, BlockHitResult $$1, BlockState $$2, Vec3 $$3, HumanoidArm $$4) {
        double $$5 = 3.0;
        int $$6 = $$4 == HumanoidArm.RIGHT ? 1 : -1;
        int $$7 = $$0.getRandom().nextInt(7, 12);
        BlockParticleOption $$8 = new BlockParticleOption(ParticleTypes.BLOCK, $$2);
        Direction $$9 = $$1.getDirection();
        DustParticlesDelta $$10 = DustParticlesDelta.fromDirection($$3, $$9);
        Vec3 $$11 = $$1.getLocation();
        for (int $$12 = 0; $$12 < $$7; ++$$12) {
            $$0.addParticle($$8, $$11.x - (double)($$9 == Direction.WEST ? 1.0E-6f : 0.0f), $$11.y, $$11.z - (double)($$9 == Direction.NORTH ? 1.0E-6f : 0.0f), $$10.xd() * (double)$$6 * 3.0 * $$0.getRandom().nextDouble(), 0.0, $$10.zd() * (double)$$6 * 3.0 * $$0.getRandom().nextDouble());
        }
    }

    record DustParticlesDelta(double xd, double yd, double zd) {
        private static final double ALONG_SIDE_DELTA = 1.0;
        private static final double OUT_FROM_SIDE_DELTA = 0.1;

        public static DustParticlesDelta fromDirection(Vec3 $$0, Direction $$1) {
            double $$2 = 0.0;
            return switch ($$1) {
                default -> throw new MatchException(null, null);
                case Direction.DOWN, Direction.UP -> new DustParticlesDelta($$0.z(), 0.0, -$$0.x());
                case Direction.NORTH -> new DustParticlesDelta(1.0, 0.0, -0.1);
                case Direction.SOUTH -> new DustParticlesDelta(-1.0, 0.0, 0.1);
                case Direction.WEST -> new DustParticlesDelta(-0.1, 0.0, -1.0);
                case Direction.EAST -> new DustParticlesDelta(0.1, 0.0, 1.0);
            };
        }
    }
}

