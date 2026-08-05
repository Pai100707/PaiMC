/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 */
package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.gossip.GossipType;

public class GossipContainer {
    public static final Codec<GossipContainer> CODEC = GossipEntry.CODEC.listOf().xmap(GossipContainer::new, $$0 -> $$0.unpack().toList());
    public static final int DISCARD_THRESHOLD = 2;
    private final Map<UUID, EntityGossips> gossips = new HashMap<UUID, EntityGossips>();

    public GossipContainer() {
    }

    private GossipContainer(List<GossipEntry> $$02) {
        $$02.forEach($$0 -> this.getOrCreate((UUID)$$0.target).entries.put((Object)$$0.type, $$0.value));
    }

    @VisibleForDebug
    public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
        HashMap $$0 = Maps.newHashMap();
        this.gossips.keySet().forEach($$1 -> {
            EntityGossips $$2 = this.gossips.get($$1);
            $$0.put($$1, $$2.entries);
        });
        return $$0;
    }

    public void decay() {
        Iterator<EntityGossips> $$0 = this.gossips.values().iterator();
        while ($$0.hasNext()) {
            EntityGossips $$1 = $$0.next();
            $$1.decay();
            if (!$$1.isEmpty()) continue;
            $$0.remove();
        }
    }

    private Stream<GossipEntry> unpack() {
        return this.gossips.entrySet().stream().flatMap($$0 -> ((EntityGossips)$$0.getValue()).unpack((UUID)$$0.getKey()));
    }

    private Collection<GossipEntry> selectGossipsForTransfer(RandomSource $$0, int $$1) {
        List<GossipEntry> $$2 = this.unpack().toList();
        if ($$2.isEmpty()) {
            return Collections.emptyList();
        }
        int[] $$3 = new int[$$2.size()];
        int $$4 = 0;
        for (int $$5 = 0; $$5 < $$2.size(); ++$$5) {
            GossipEntry $$6 = $$2.get($$5);
            $$3[$$5] = ($$4 += Math.abs($$6.weightedValue())) - 1;
        }
        Set $$7 = Sets.newIdentityHashSet();
        for (int $$8 = 0; $$8 < $$1; ++$$8) {
            int $$9 = $$0.nextInt($$4);
            int $$10 = Arrays.binarySearch($$3, $$9);
            $$7.add($$2.get($$10 < 0 ? -$$10 - 1 : $$10));
        }
        return $$7;
    }

    private EntityGossips getOrCreate(UUID $$02) {
        return this.gossips.computeIfAbsent($$02, $$0 -> new EntityGossips());
    }

    public void transferFrom(GossipContainer $$02, RandomSource $$1, int $$2) {
        Collection<GossipEntry> $$3 = $$02.selectGossipsForTransfer($$1, $$2);
        $$3.forEach($$0 -> {
            int $$1 = $$0.value - $$0.type.decayPerTransfer;
            if ($$1 >= 2) {
                this.getOrCreate((UUID)$$0.target).entries.mergeInt((Object)$$0.type, $$1, GossipContainer::mergeValuesForTransfer);
            }
        });
    }

    public int getReputation(UUID $$0, Predicate<GossipType> $$1) {
        EntityGossips $$2 = this.gossips.get($$0);
        return $$2 != null ? $$2.weightedValue($$1) : 0;
    }

    public long getCountForType(GossipType $$0, DoublePredicate $$1) {
        return this.gossips.values().stream().filter($$2 -> $$1.test($$2.entries.getOrDefault((Object)$$0, 0) * $$1.weight)).count();
    }

    public void add(UUID $$0, GossipType $$12, int $$22) {
        EntityGossips $$3 = this.getOrCreate($$0);
        $$3.entries.mergeInt((Object)$$12, $$22, ($$1, $$2) -> this.mergeValuesForAddition($$12, $$1, $$2));
        $$3.makeSureValueIsntTooLowOrTooHigh($$12);
        if ($$3.isEmpty()) {
            this.gossips.remove($$0);
        }
    }

    public void remove(UUID $$0, GossipType $$1, int $$2) {
        this.add($$0, $$1, -$$2);
    }

    public void remove(UUID $$0, GossipType $$1) {
        EntityGossips $$2 = this.gossips.get($$0);
        if ($$2 != null) {
            $$2.remove($$1);
            if ($$2.isEmpty()) {
                this.gossips.remove($$0);
            }
        }
    }

    public void remove(GossipType $$0) {
        Iterator<EntityGossips> $$1 = this.gossips.values().iterator();
        while ($$1.hasNext()) {
            EntityGossips $$2 = $$1.next();
            $$2.remove($$0);
            if (!$$2.isEmpty()) continue;
            $$1.remove();
        }
    }

    public void clear() {
        this.gossips.clear();
    }

    public void putAll(GossipContainer $$02) {
        $$02.gossips.forEach(($$0, $$1) -> this.getOrCreate((UUID)$$0).entries.putAll($$1.entries));
    }

    private static int mergeValuesForTransfer(int $$0, int $$1) {
        return Math.max($$0, $$1);
    }

    private int mergeValuesForAddition(GossipType $$0, int $$1, int $$2) {
        int $$3 = $$1 + $$2;
        return $$3 > $$0.max ? Math.max($$0.max, $$1) : $$3;
    }

    public GossipContainer copy() {
        GossipContainer $$0 = new GossipContainer();
        $$0.putAll(this);
        return $$0;
    }

    static class EntityGossips {
        final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap();

        EntityGossips() {
        }

        public int weightedValue(Predicate<GossipType> $$02) {
            return this.entries.object2IntEntrySet().stream().filter($$1 -> $$02.test((GossipType)$$1.getKey())).mapToInt($$0 -> $$0.getIntValue() * ((GossipType)$$0.getKey()).weight).sum();
        }

        public Stream<GossipEntry> unpack(UUID $$0) {
            return this.entries.object2IntEntrySet().stream().map($$1 -> new GossipEntry($$0, (GossipType)$$1.getKey(), $$1.getIntValue()));
        }

        public void decay() {
            ObjectIterator $$0 = this.entries.object2IntEntrySet().iterator();
            while ($$0.hasNext()) {
                Object2IntMap.Entry $$1 = (Object2IntMap.Entry)$$0.next();
                int $$2 = $$1.getIntValue() - ((GossipType)$$1.getKey()).decayPerDay;
                if ($$2 < 2) {
                    $$0.remove();
                    continue;
                }
                $$1.setValue($$2);
            }
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(GossipType $$0) {
            int $$1 = this.entries.getInt((Object)$$0);
            if ($$1 > $$0.max) {
                this.entries.put((Object)$$0, $$0.max);
            }
            if ($$1 < 2) {
                this.remove($$0);
            }
        }

        public void remove(GossipType $$0) {
            this.entries.removeInt((Object)$$0);
        }
    }

    static final class GossipEntry
    extends Record {
        final UUID target;
        final GossipType type;
        final int value;
        public static final Codec<GossipEntry> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipEntry::target), (App)GossipType.CODEC.fieldOf("Type").forGetter(GossipEntry::type), (App)ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipEntry::value)).apply((Applicative)$$0, GossipEntry::new));

        GossipEntry(UUID $$0, GossipType $$1, int $$2) {
            this.target = $$0;
            this.type = $$1;
            this.value = $$2;
        }

        public int weightedValue() {
            return this.value * this.type.weight;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GossipEntry.class, "target;type;value", "target", "type", "value"}, this, $$0);
        }

        public UUID target() {
            return this.target;
        }

        public GossipType type() {
            return this.type;
        }

        public int value() {
            return this.value;
        }
    }
}

