/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.server.level;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum ParticleStatus {
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID;
    public static final Codec<ParticleStatus> LEGACY_CODEC;
    private final int id;
    private final Component caption;

    private ParticleStatus(int $$0, String $$1) {
        this.id = $$0;
        this.caption = Component.translatable($$1);
    }

    public Component caption() {
        return this.caption;
    }

    static {
        BY_ID = ByIdMap.continuous($$0 -> $$0.id, ParticleStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, $$0 -> $$0.id);
    }
}

