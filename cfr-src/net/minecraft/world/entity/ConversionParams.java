/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public record ConversionParams(ConversionType type, boolean keepEquipment, boolean preserveCanPickUpLoot, @Nullable PlayerTeam team) {
    public static ConversionParams single(Mob $$0, boolean $$1, boolean $$2) {
        return new ConversionParams(ConversionType.SINGLE, $$1, $$2, $$0.getTeam());
    }

    @FunctionalInterface
    public static interface AfterConversion<T extends Mob> {
        public void finalizeConversion(T var1);
    }
}

