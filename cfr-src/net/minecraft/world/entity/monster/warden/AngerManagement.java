/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class AngerManagement {
    @VisibleForTesting
    protected static final int CONVERSION_DELAY = 2;
    @VisibleForTesting
    protected static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
    int highestAnger;
    private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create($$0 -> $$0.group((App)UUIDUtil.CODEC.fieldOf("uuid").forGetter(Pair::getFirst), (App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)).apply((Applicative)$$0, Pair::of));
    private final Predicate<Entity> filter;
    @VisibleForTesting
    protected final ArrayList<Entity> suspects;
    private final Sorter suspectSorter;
    @VisibleForTesting
    protected final Object2IntMap<Entity> angerBySuspect;
    @VisibleForTesting
    protected final Object2IntMap<UUID> angerByUuid;

    public static Codec<AngerManagement> codec(Predicate<Entity> $$0) {
        return RecordCodecBuilder.create($$12 -> $$12.group((App)SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs)).apply((Applicative)$$12, $$1 -> new AngerManagement($$0, (List<Pair<UUID, Integer>>)$$1)));
    }

    public AngerManagement(Predicate<Entity> $$02, List<Pair<UUID, Integer>> $$1) {
        this.filter = $$02;
        this.suspects = new ArrayList();
        this.suspectSorter = new Sorter(this);
        this.angerBySuspect = new Object2IntOpenHashMap();
        this.angerByUuid = new Object2IntOpenHashMap($$1.size());
        $$1.forEach($$0 -> this.angerByUuid.put((Object)((UUID)$$0.getFirst()), (Integer)$$0.getSecond()));
    }

    private List<Pair<UUID, Integer>> createUuidAngerPairs() {
        return Streams.concat((Stream[])new Stream[]{this.suspects.stream().map($$0 -> Pair.of((Object)$$0.getUUID(), (Object)this.angerBySuspect.getInt($$0))), this.angerByUuid.object2IntEntrySet().stream().map($$0 -> Pair.of((Object)((UUID)$$0.getKey()), (Object)$$0.getIntValue()))}).collect(Collectors.toList());
    }

    public void tick(ServerLevel $$0, Predicate<Entity> $$1) {
        --this.conversionDelay;
        if (this.conversionDelay <= 0) {
            this.convertFromUuids($$0);
            this.conversionDelay = 2;
        }
        ObjectIterator $$2 = this.angerByUuid.object2IntEntrySet().iterator();
        while ($$2.hasNext()) {
            Object2IntMap.Entry $$3 = (Object2IntMap.Entry)$$2.next();
            int $$4 = $$3.getIntValue();
            if ($$4 <= 1) {
                $$2.remove();
                continue;
            }
            $$3.setValue($$4 - 1);
        }
        ObjectIterator $$5 = this.angerBySuspect.object2IntEntrySet().iterator();
        while ($$5.hasNext()) {
            Object2IntMap.Entry $$6 = (Object2IntMap.Entry)$$5.next();
            int $$7 = $$6.getIntValue();
            Entity $$8 = (Entity)$$6.getKey();
            Entity.RemovalReason $$9 = $$8.getRemovalReason();
            if ($$7 <= 1 || !$$1.test($$8) || $$9 != null) {
                this.suspects.remove($$8);
                $$5.remove();
                if ($$7 <= 1 || $$9 == null) continue;
                switch ($$9) {
                    case CHANGED_DIMENSION: 
                    case UNLOADED_TO_CHUNK: 
                    case UNLOADED_WITH_PLAYER: {
                        this.angerByUuid.put((Object)$$8.getUUID(), $$7 - 1);
                    }
                }
                continue;
            }
            $$6.setValue($$7 - 1);
        }
        this.sortAndUpdateHighestAnger();
    }

    private void sortAndUpdateHighestAnger() {
        this.highestAnger = 0;
        this.suspects.sort(this.suspectSorter);
        if (this.suspects.size() == 1) {
            this.highestAnger = this.angerBySuspect.getInt((Object)this.suspects.get(0));
        }
    }

    private void convertFromUuids(ServerLevel $$0) {
        ObjectIterator $$1 = this.angerByUuid.object2IntEntrySet().iterator();
        while ($$1.hasNext()) {
            Object2IntMap.Entry $$2 = (Object2IntMap.Entry)$$1.next();
            int $$3 = $$2.getIntValue();
            Entity $$4 = $$0.getEntity((UUID)$$2.getKey());
            if ($$4 == null) continue;
            this.angerBySuspect.put((Object)$$4, $$3);
            this.suspects.add($$4);
            $$1.remove();
        }
    }

    public int increaseAnger(Entity $$0, int $$12) {
        boolean $$22 = !this.angerBySuspect.containsKey((Object)$$0);
        int $$3 = this.angerBySuspect.computeInt((Object)$$0, ($$1, $$2) -> Math.min(150, ($$2 == null ? 0 : $$2) + $$12));
        if ($$22) {
            int $$4 = this.angerByUuid.removeInt((Object)$$0.getUUID());
            this.angerBySuspect.put((Object)$$0, $$3 += $$4);
            this.suspects.add($$0);
        }
        this.sortAndUpdateHighestAnger();
        return $$3;
    }

    public void clearAnger(Entity $$0) {
        this.angerBySuspect.removeInt((Object)$$0);
        this.suspects.remove($$0);
        this.sortAndUpdateHighestAnger();
    }

    private @Nullable Entity getTopSuspect() {
        return this.suspects.stream().filter(this.filter).findFirst().orElse(null);
    }

    public int getActiveAnger(@Nullable Entity $$0) {
        return $$0 == null ? this.highestAnger : this.angerBySuspect.getInt((Object)$$0);
    }

    public Optional<LivingEntity> getActiveEntity() {
        return Optional.ofNullable(this.getTopSuspect()).filter($$0 -> $$0 instanceof LivingEntity).map($$0 -> (LivingEntity)$$0);
    }

    @VisibleForTesting
    protected record Sorter(AngerManagement angerManagement) implements Comparator<Entity>
    {
        @Override
        public int compare(Entity $$0, Entity $$1) {
            boolean $$5;
            if ($$0.equals($$1)) {
                return 0;
            }
            int $$2 = this.angerManagement.angerBySuspect.getOrDefault((Object)$$0, 0);
            int $$3 = this.angerManagement.angerBySuspect.getOrDefault((Object)$$1, 0);
            this.angerManagement.highestAnger = Math.max(this.angerManagement.highestAnger, Math.max($$2, $$3));
            boolean $$4 = AngerLevel.byAnger($$2).isAngry();
            if ($$4 != ($$5 = AngerLevel.byAnger($$3).isAngry())) {
                return $$4 ? -1 : 1;
            }
            boolean $$6 = $$0 instanceof Player;
            boolean $$7 = $$1 instanceof Player;
            if ($$6 != $$7) {
                return $$6 ? -1 : 1;
            }
            return Integer.compare($$3, $$2);
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((Entity)object, (Entity)object2);
        }
    }
}

