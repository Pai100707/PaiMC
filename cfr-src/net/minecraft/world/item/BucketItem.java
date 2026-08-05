/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class BucketItem
extends Item
implements DispensibleContainerItem {
    private final Fluid content;

    public BucketItem(Fluid $$0, Item.Properties $$1) {
        super($$1);
        this.content = $$0;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$12, InteractionHand $$2) {
        ItemStack $$3 = $$12.getItemInHand($$2);
        BlockHitResult $$4 = BucketItem.getPlayerPOVHitResult($$0, $$12, this.content == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if ($$4.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        if ($$4.getType() == HitResult.Type.BLOCK) {
            BlockPos $$13;
            BlockPos $$5 = $$4.getBlockPos();
            Direction $$6 = $$4.getDirection();
            BlockPos $$7 = $$5.relative($$6);
            if (!$$0.mayInteract($$12, $$5) || !$$12.mayUseItemAt($$7, $$6, $$3)) {
                return InteractionResult.FAIL;
            }
            if (this.content == Fluids.EMPTY) {
                BucketPickup $$9;
                ItemStack $$10;
                BlockState $$8 = $$0.getBlockState($$5);
                Block block = $$8.getBlock();
                if (block instanceof BucketPickup && !($$10 = ($$9 = (BucketPickup)((Object)block)).pickupBlock($$12, $$0, $$5, $$8)).isEmpty()) {
                    $$12.awardStat(Stats.ITEM_USED.get(this));
                    $$9.getPickupSound().ifPresent($$1 -> $$12.playSound((SoundEvent)$$1, 1.0f, 1.0f));
                    $$0.gameEvent((Entity)$$12, GameEvent.FLUID_PICKUP, $$5);
                    ItemStack $$11 = ItemUtils.createFilledResult($$3, $$12, $$10);
                    if (!$$0.isClientSide()) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)$$12, $$10);
                    }
                    return InteractionResult.SUCCESS.heldItemTransformedTo($$11);
                }
                return InteractionResult.FAIL;
            }
            BlockState $$122 = $$0.getBlockState($$5);
            BlockPos blockPos = $$13 = $$122.getBlock() instanceof LiquidBlockContainer && this.content == Fluids.WATER ? $$5 : $$7;
            if (this.emptyContents($$12, $$0, $$13, $$4)) {
                this.checkExtraContent($$12, $$0, $$3, $$13);
                if ($$12 instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)$$12, $$13, $$3);
                }
                $$12.awardStat(Stats.ITEM_USED.get(this));
                ItemStack $$14 = ItemUtils.createFilledResult($$3, $$12, BucketItem.getEmptySuccessItem($$3, $$12));
                return InteractionResult.SUCCESS.heldItemTransformedTo($$14);
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static ItemStack getEmptySuccessItem(ItemStack $$0, Player $$1) {
        if (!$$1.hasInfiniteMaterials()) {
            return new ItemStack(Items.BUCKET);
        }
        return $$0;
    }

    @Override
    public void checkExtraContent(@Nullable LivingEntity $$0, Level $$1, ItemStack $$2, BlockPos $$3) {
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean emptyContents(@Nullable LivingEntity $$0, Level $$1, BlockPos $$2, @Nullable BlockHitResult $$3) {
        boolean $$12;
        LiquidBlockContainer $$10;
        Fluid fluid = this.content;
        if (!(fluid instanceof FlowingFluid)) {
            return false;
        }
        FlowingFluid $$4 = (FlowingFluid)fluid;
        BlockState $$6 = $$1.getBlockState($$2);
        Block $$7 = $$6.getBlock();
        boolean $$8 = $$6.canBeReplaced(this.content);
        boolean $$9 = $$0 != null && $$0.isShiftKeyDown();
        boolean $$11 = $$8 || $$7 instanceof LiquidBlockContainer && ($$10 = (LiquidBlockContainer)((Object)$$7)).canPlaceLiquid($$0, $$1, $$2, $$6, this.content);
        boolean bl = $$12 = $$6.isAir() || $$11 && (!$$9 || $$3 == null);
        if (!$$12) {
            return $$3 != null && this.emptyContents($$0, $$1, $$3.getBlockPos().relative($$3.getDirection()), null);
        }
        if ($$1.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, $$2).booleanValue() && this.content.is(FluidTags.WATER)) {
            int $$13 = $$2.getX();
            int $$14 = $$2.getY();
            int $$15 = $$2.getZ();
            $$1.playSound((Entity)$$0, $$2, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + ($$1.random.nextFloat() - $$1.random.nextFloat()) * 0.8f);
            for (int $$16 = 0; $$16 < 8; ++$$16) {
                $$1.addParticle(ParticleTypes.LARGE_SMOKE, (float)$$13 + $$1.random.nextFloat(), (float)$$14 + $$1.random.nextFloat(), (float)$$15 + $$1.random.nextFloat(), 0.0, 0.0, 0.0);
            }
            return true;
        }
        if ($$7 instanceof LiquidBlockContainer) {
            LiquidBlockContainer $$17 = (LiquidBlockContainer)((Object)$$7);
            if (this.content == Fluids.WATER) {
                void $$5;
                $$17.placeLiquid($$1, $$2, $$6, $$5.getSource(false));
                this.playEmptySound($$0, $$1, $$2);
                return true;
            }
        }
        if (!$$1.isClientSide() && $$8 && !$$6.liquid()) {
            $$1.destroyBlock($$2, true);
        }
        if ($$1.setBlock($$2, this.content.defaultFluidState().createLegacyBlock(), 11) || $$6.getFluidState().isSource()) {
            this.playEmptySound($$0, $$1, $$2);
            return true;
        }
        return false;
    }

    protected void playEmptySound(@Nullable LivingEntity $$0, LevelAccessor $$1, BlockPos $$2) {
        SoundEvent $$3 = this.content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        $$1.playSound($$0, $$2, $$3, SoundSource.BLOCKS, 1.0f, 1.0f);
        $$1.gameEvent((Entity)$$0, GameEvent.FLUID_PLACE, $$2);
    }

    public Fluid getContent() {
        return this.content;
    }
}

