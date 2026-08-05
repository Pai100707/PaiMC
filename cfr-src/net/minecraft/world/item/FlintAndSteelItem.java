/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class FlintAndSteelItem
extends Item {
    public FlintAndSteelItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        BlockPos $$3;
        Player $$1 = $$0.getPlayer();
        Level $$2 = $$0.getLevel();
        BlockState $$4 = $$2.getBlockState($$3 = $$0.getClickedPos());
        if (CampfireBlock.canLight($$4) || CandleBlock.canLight($$4) || CandleCakeBlock.canLight($$4)) {
            $$2.playSound((Entity)$$1, $$3, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, $$2.getRandom().nextFloat() * 0.4f + 0.8f);
            $$2.setBlock($$3, (BlockState)$$4.setValue(BlockStateProperties.LIT, true), 11);
            $$2.gameEvent((Entity)$$1, GameEvent.BLOCK_CHANGE, $$3);
            if ($$1 != null) {
                $$0.getItemInHand().hurtAndBreak(1, (LivingEntity)$$1, $$0.getHand().asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        BlockPos $$5 = $$3.relative($$0.getClickedFace());
        if (BaseFireBlock.canBePlacedAt($$2, $$5, $$0.getHorizontalDirection())) {
            $$2.playSound((Entity)$$1, $$5, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, $$2.getRandom().nextFloat() * 0.4f + 0.8f);
            BlockState $$6 = BaseFireBlock.getState($$2, $$5);
            $$2.setBlock($$5, $$6, 11);
            $$2.gameEvent((Entity)$$1, GameEvent.BLOCK_PLACE, $$3);
            ItemStack $$7 = $$0.getItemInHand();
            if ($$1 instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)$$1, $$5, $$7);
                $$7.hurtAndBreak(1, (LivingEntity)$$1, $$0.getHand().asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}

