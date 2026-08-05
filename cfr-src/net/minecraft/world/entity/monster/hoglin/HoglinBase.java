/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.hoglin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public interface HoglinBase {
    public static final int ATTACK_ANIMATION_DURATION = 10;
    public static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2f;

    public int getAttackAnimationRemainingTicks();

    public static boolean hurtAndThrowTarget(ServerLevel $$0, LivingEntity $$1, LivingEntity $$2) {
        float $$5;
        float $$3 = (float)$$1.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (!$$1.isBaby() && (int)$$3 > 0) {
            float $$4 = $$3 / 2.0f + (float)$$0.random.nextInt((int)$$3);
        } else {
            $$5 = $$3;
        }
        DamageSource $$6 = $$1.damageSources().mobAttack($$1);
        boolean $$7 = $$2.hurtServer($$0, $$6, $$5);
        if ($$7) {
            EnchantmentHelper.doPostAttackEffects($$0, $$2, $$6);
            if (!$$1.isBaby()) {
                HoglinBase.throwTarget($$1, $$2);
            }
        }
        return $$7;
    }

    public static void throwTarget(LivingEntity $$0, LivingEntity $$1) {
        double $$3;
        double $$2 = $$0.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        double $$4 = $$2 - ($$3 = $$1.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if ($$4 <= 0.0) {
            return;
        }
        double $$5 = $$1.getX() - $$0.getX();
        double $$6 = $$1.getZ() - $$0.getZ();
        float $$7 = $$0.level().random.nextInt(21) - 10;
        double $$8 = $$4 * (double)($$0.level().random.nextFloat() * 0.5f + 0.2f);
        Vec3 $$9 = new Vec3($$5, 0.0, $$6).normalize().scale($$8).yRot($$7);
        double $$10 = $$4 * (double)$$0.level().random.nextFloat() * 0.5;
        $$1.push($$9.x, $$10, $$9.z);
        $$1.hurtMarked = true;
    }
}

