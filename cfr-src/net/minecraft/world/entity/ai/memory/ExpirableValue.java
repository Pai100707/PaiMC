/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.ai.memory;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue<T> {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T $$0, long $$1) {
        this.value = $$0;
        this.timeToLive = $$1;
    }

    public void tick() {
        if (this.canExpire()) {
            --this.timeToLive;
        }
    }

    public static <T> ExpirableValue<T> of(T $$0) {
        return new ExpirableValue<T>($$0, Long.MAX_VALUE);
    }

    public static <T> ExpirableValue<T> of(T $$0, long $$1) {
        return new ExpirableValue<T>($$0, $$1);
    }

    public long getTimeToLive() {
        return this.timeToLive;
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    public String toString() {
        return String.valueOf(this.value) + (String)(this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    @VisibleForDebug
    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    public static <T> Codec<ExpirableValue<T>> codec(Codec<T> $$0) {
        return RecordCodecBuilder.create($$12 -> $$12.group((App)$$0.fieldOf("value").forGetter($$0 -> $$0.value), (App)Codec.LONG.lenientOptionalFieldOf("ttl").forGetter($$0 -> $$0.canExpire() ? Optional.of($$0.timeToLive) : Optional.empty())).apply((Applicative)$$12, ($$0, $$1) -> new ExpirableValue<Object>($$0, $$1.orElse(Long.MAX_VALUE))));
    }
}

