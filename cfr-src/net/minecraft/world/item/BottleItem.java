/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BottleItem
extends Item {
    public BottleItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult use(Level $$02, Player $$1, InteractionHand $$2) {
        List<AreaEffectCloud> $$3 = $$02.getEntitiesOfClass(AreaEffectCloud.class, $$1.getBoundingBox().inflate(2.0), $$0 -> $$0.isAlive() && $$0.getOwner() instanceof EnderDragon);
        ItemStack $$4 = $$1.getItemInHand($$2);
        if (!$$3.isEmpty()) {
            AreaEffectCloud $$5 = $$3.get(0);
            $$5.setRadius($$5.getRadius() - 0.5f);
            $$02.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0f, 1.0f);
            $$02.gameEvent((Entity)$$1, GameEvent.FLUID_PICKUP, $$1.position());
            if ($$1 instanceof ServerPlayer) {
                ServerPlayer $$6 = (ServerPlayer)$$1;
                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger($$6, $$4, $$5);
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(this.turnBottleIntoItem($$4, $$1, new ItemStack(Items.DRAGON_BREATH)));
        }
        BlockHitResult $$7 = BottleItem.getPlayerPOVHitResult($$02, $$1, ClipContext.Fluid.SOURCE_ONLY);
        if ($$7.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        if ($$7.getType() == HitResult.Type.BLOCK) {
            BlockPos $$8 = $$7.getBlockPos();
            if (!$$02.mayInteract($$1, $$8)) {
                return InteractionResult.PASS;
            }
            if ($$02.getFluidState($$8).is(FluidTags.WATER)) {
                $$02.playSound((Entity)$$1, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                $$02.gameEvent((Entity)$$1, GameEvent.FLUID_PICKUP, $$8);
                return InteractionResult.SUCCESS.heldItemTransformedTo(this.turnBottleIntoItem($$4, $$1, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
            }
        }
        return InteractionResult.PASS;
    }

    protected ItemStack turnBottleIntoItem(ItemStack $$0, Player $$1, ItemStack $$2) {
        $$1.awardStat(Stats.ITEM_USED.get(this));
        return ItemUtils.createFilledResult($$0, $$1, $$2);
    }
}

