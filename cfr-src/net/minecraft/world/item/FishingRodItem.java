/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FishingRodItem
extends Item {
    public FishingRodItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        if ($$1.fishing != null) {
            if (!$$0.isClientSide()) {
                int $$4 = $$1.fishing.retrieve($$3);
                $$3.hurtAndBreak($$4, (LivingEntity)$$1, $$2.asEquipmentSlot());
            }
            $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0f, 0.4f / ($$0.getRandom().nextFloat() * 0.4f + 0.8f));
            $$3.causeUseVibration($$1, GameEvent.ITEM_INTERACT_FINISH);
        } else {
            $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / ($$0.getRandom().nextFloat() * 0.4f + 0.8f));
            if ($$0 instanceof ServerLevel) {
                ServerLevel $$5 = (ServerLevel)$$0;
                int $$6 = (int)(EnchantmentHelper.getFishingTimeReduction($$5, $$3, $$1) * 20.0f);
                int $$7 = EnchantmentHelper.getFishingLuckBonus($$5, $$3, $$1);
                Projectile.spawnProjectile(new FishingHook($$1, $$0, $$7, $$6), $$5, $$3);
            }
            $$1.awardStat(Stats.ITEM_USED.get(this));
            $$3.causeUseVibration($$1, GameEvent.ITEM_INTERACT_START);
        }
        return InteractionResult.SUCCESS;
    }
}

