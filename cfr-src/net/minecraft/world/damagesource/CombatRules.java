/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.damagesource;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class CombatRules {
    public static final float MAX_ARMOR = 20.0f;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0f;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0f;
    public static final float MIN_ARMOR_RATIO = 0.2f;
    private static final int NUM_ARMOR_ITEMS = 4;

    public static float getDamageAfterAbsorb(LivingEntity $$0, float $$1, DamageSource $$2, float $$3, float $$4) {
        float $$11;
        Level level;
        float $$5 = 2.0f + $$4 / 4.0f;
        float $$6 = Mth.clamp($$3 - $$1 / $$5, $$3 * 0.2f, 20.0f);
        float $$7 = $$6 / 25.0f;
        ItemStack $$8 = $$2.getWeaponItem();
        if ($$8 != null && (level = $$0.level()) instanceof ServerLevel) {
            ServerLevel $$9 = (ServerLevel)level;
            float $$10 = Mth.clamp(EnchantmentHelper.modifyArmorEffectiveness($$9, $$8, $$0, $$2, $$7), 0.0f, 1.0f);
        } else {
            $$11 = $$7;
        }
        float $$12 = 1.0f - $$11;
        return $$1 * $$12;
    }

    public static float getDamageAfterMagicAbsorb(float $$0, float $$1) {
        float $$2 = Mth.clamp($$1, 0.0f, 20.0f);
        return $$0 * (1.0f - $$2 / 25.0f);
    }
}

