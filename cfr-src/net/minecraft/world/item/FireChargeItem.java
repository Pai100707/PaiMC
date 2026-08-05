/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class FireChargeItem
extends Item
implements ProjectileItem {
    public FireChargeItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        Level $$1 = $$0.getLevel();
        BlockPos $$2 = $$0.getClickedPos();
        BlockState $$3 = $$1.getBlockState($$2);
        boolean $$4 = false;
        if (CampfireBlock.canLight($$3) || CandleBlock.canLight($$3) || CandleCakeBlock.canLight($$3)) {
            this.playSound($$1, $$2);
            $$1.setBlockAndUpdate($$2, (BlockState)$$3.setValue(BlockStateProperties.LIT, true));
            $$1.gameEvent((Entity)$$0.getPlayer(), GameEvent.BLOCK_CHANGE, $$2);
            $$4 = true;
        } else if (BaseFireBlock.canBePlacedAt($$1, $$2 = $$2.relative($$0.getClickedFace()), $$0.getHorizontalDirection())) {
            this.playSound($$1, $$2);
            $$1.setBlockAndUpdate($$2, BaseFireBlock.getState($$1, $$2));
            $$1.gameEvent((Entity)$$0.getPlayer(), GameEvent.BLOCK_PLACE, $$2);
            $$4 = true;
        }
        if ($$4) {
            $$0.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private void playSound(Level $$0, BlockPos $$1) {
        RandomSource $$2 = $$0.getRandom();
        $$0.playSound(null, $$1, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, ($$2.nextFloat() - $$2.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public Projectile asProjectile(Level $$0, Position $$1, ItemStack $$2, Direction $$3) {
        RandomSource $$4 = $$0.getRandom();
        double $$5 = $$4.triangle((double)$$3.getStepX(), 0.11485000000000001);
        double $$6 = $$4.triangle((double)$$3.getStepY(), 0.11485000000000001);
        double $$7 = $$4.triangle((double)$$3.getStepZ(), 0.11485000000000001);
        Vec3 $$8 = new Vec3($$5, $$6, $$7);
        SmallFireball $$9 = new SmallFireball($$0, $$1.x(), $$1.y(), $$1.z(), $$8.normalize());
        $$9.setItem($$2);
        return $$9;
    }

    @Override
    public void shoot(Projectile $$0, double $$1, double $$2, double $$3, float $$4, float $$5) {
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder().positionFunction(($$0, $$1) -> DispenserBlock.getDispensePosition($$0, 1.0, Vec3.ZERO)).uncertainty(6.6666665f).power(1.0f).overrideDispenseEvent(1018).build();
    }
}

