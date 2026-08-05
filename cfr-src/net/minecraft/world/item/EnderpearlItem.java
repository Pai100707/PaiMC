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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnderpearlItem
extends Item {
    public static float PROJECTILE_SHOOT_POWER = 1.5f;

    public EnderpearlItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / ($$0.getRandom().nextFloat() * 0.4f + 0.8f));
        if ($$0 instanceof ServerLevel) {
            ServerLevel $$4 = (ServerLevel)$$0;
            Projectile.spawnProjectileFromRotation(ThrownEnderpearl::new, $$4, $$3, $$1, 0.0f, PROJECTILE_SHOOT_POWER, 1.0f);
        }
        $$1.awardStat(Stats.ITEM_USED.get(this));
        $$3.consume(1, $$1);
        return InteractionResult.SUCCESS;
    }
}

