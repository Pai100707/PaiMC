/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface EntityProcessor {
    public static final EntityProcessor NOP = $$0 -> $$0;

    public @Nullable Entity process(Entity var1);
}

