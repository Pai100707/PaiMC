/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;

public class StatType<T>
implements Iterable<Stat<T>> {
    private final Registry<T> registry;
    private final Map<T, Stat<T>> map = new IdentityHashMap<T, Stat<T>>();
    private final Component displayName;
    private final StreamCodec<RegistryFriendlyByteBuf, Stat<T>> streamCodec;

    public StatType(Registry<T> $$0, Component $$1) {
        this.registry = $$0;
        this.displayName = $$1;
        this.streamCodec = ByteBufCodecs.registry($$0.key()).map(this::get, Stat::getValue);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Stat<T>> streamCodec() {
        return this.streamCodec;
    }

    public boolean contains(T $$0) {
        return this.map.containsKey($$0);
    }

    public Stat<T> get(T $$0, StatFormatter $$12) {
        return this.map.computeIfAbsent($$0, $$1 -> new Stat<Object>(this, $$1, $$12));
    }

    public Registry<T> getRegistry() {
        return this.registry;
    }

    @Override
    public Iterator<Stat<T>> iterator() {
        return this.map.values().iterator();
    }

    public Stat<T> get(T $$0) {
        return this.get($$0, StatFormatter.DEFAULT);
    }

    public Component getDisplayName() {
        return this.displayName;
    }
}

