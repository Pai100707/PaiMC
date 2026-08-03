package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
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

public class GossipContainer {
   public static final Codec<GossipContainer> CODEC = GossipContainer.GossipEntry.CODEC.listOf().xmap(GossipContainer::new, $$0 -> $$0.unpack().toList());
   public static final int DISCARD_THRESHOLD = 2;
   private final Map<UUID, GossipContainer.EntityGossips> gossips = new HashMap<>();

   public GossipContainer() {
   }

   private GossipContainer(List<GossipContainer.GossipEntry> $$0) {
      $$0.forEach($$0x -> this.getOrCreate($$0x.target).entries.put($$0x.type, $$0x.value));
   }

   @VisibleForDebug
   public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
      Map<UUID, Object2IntMap<GossipType>> $$0 = Maps.newHashMap();
      this.gossips.keySet().forEach($$1 -> {
         GossipContainer.EntityGossips $$2 = this.gossips.get($$1);
         $$0.put($$1, $$2.entries);
      });
      return $$0;
   }

   public void decay() {
      Iterator<GossipContainer.EntityGossips> $$0 = this.gossips.values().iterator();

      while ($$0.hasNext()) {
         GossipContainer.EntityGossips $$1 = $$0.next();
         $$1.decay();
         if ($$1.isEmpty()) {
            $$0.remove();
         }
      }
   }

   private Stream<GossipContainer.GossipEntry> unpack() {
      return this.gossips.entrySet().stream().flatMap($$0 -> $$0.getValue().unpack($$0.getKey()));
   }

   private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(RandomSource $$0, int $$1) {
      List<GossipContainer.GossipEntry> $$2 = this.unpack().toList();
      if ($$2.isEmpty()) {
         return Collections.emptyList();
      } else {
         int[] $$3 = new int[$$2.size()];
         int $$4 = 0;

         for (int $$5 = 0; $$5 < $$2.size(); $$5++) {
            GossipContainer.GossipEntry $$6 = $$2.get($$5);
            $$4 += Math.abs($$6.weightedValue());
            $$3[$$5] = $$4 - 1;
         }

         Set<GossipContainer.GossipEntry> $$7 = Sets.newIdentityHashSet();

         for (int $$8 = 0; $$8 < $$1; $$8++) {
            int $$9 = $$0.nextInt($$4);
            int $$10 = Arrays.binarySearch($$3, $$9);
            $$7.add($$2.get($$10 < 0 ? -$$10 - 1 : $$10));
         }

         return $$7;
      }
   }

   private GossipContainer.EntityGossips getOrCreate(UUID $$0) {
      return this.gossips.computeIfAbsent($$0, $$0x -> new GossipContainer.EntityGossips());
   }

   public void transferFrom(GossipContainer $$0, RandomSource $$1, int $$2) {
      Collection<GossipContainer.GossipEntry> $$3 = $$0.selectGossipsForTransfer($$1, $$2);
      $$3.forEach($$0x -> {
         int $$1x = $$0x.value - $$0x.type.decayPerTransfer;
         if ($$1x >= 2) {
            this.getOrCreate($$0x.target).entries.mergeInt($$0x.type, $$1x, GossipContainer::mergeValuesForTransfer);
         }
      });
   }

   public int getReputation(UUID $$0, Predicate<GossipType> $$1) {
      GossipContainer.EntityGossips $$2 = this.gossips.get($$0);
      return $$2 != null ? $$2.weightedValue($$1) : 0;
   }

   public long getCountForType(GossipType $$0, DoublePredicate $$1) {
      return this.gossips.values().stream().filter($$2 -> $$1.test($$2.entries.getOrDefault($$0, 0) * $$0.weight)).count();
   }

   public void add(UUID $$0, GossipType $$1, int $$2) {
      GossipContainer.EntityGossips $$3 = this.getOrCreate($$0);
      $$3.entries.mergeInt($$1, $$2, ($$1x, $$2x) -> this.mergeValuesForAddition($$1, $$1x, $$2x));
      $$3.makeSureValueIsntTooLowOrTooHigh($$1);
      if ($$3.isEmpty()) {
         this.gossips.remove($$0);
      }
   }

   public void remove(UUID $$0, GossipType $$1, int $$2) {
      this.add($$0, $$1, -$$2);
   }

   public void remove(UUID $$0, GossipType $$1) {
      GossipContainer.EntityGossips $$2 = this.gossips.get($$0);
      if ($$2 != null) {
         $$2.remove($$1);
         if ($$2.isEmpty()) {
            this.gossips.remove($$0);
         }
      }
   }

   public void remove(GossipType $$0) {
      Iterator<GossipContainer.EntityGossips> $$1 = this.gossips.values().iterator();

      while ($$1.hasNext()) {
         GossipContainer.EntityGossips $$2 = $$1.next();
         $$2.remove($$0);
         if ($$2.isEmpty()) {
            $$1.remove();
         }
      }
   }

   public void clear() {
      this.gossips.clear();
   }

   public void putAll(GossipContainer $$0) {
      $$0.gossips.forEach(($$0x, $$1) -> this.getOrCreate($$0x).entries.putAll($$1.entries));
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

      public int weightedValue(Predicate<GossipType> $$0) {
         return this.entries
            .object2IntEntrySet()
            .stream()
            .filter($$1 -> $$0.test((GossipType)$$1.getKey()))
            .mapToInt($$0x -> $$0x.getIntValue() * ((GossipType)$$0x.getKey()).weight)
            .sum();
      }

      public Stream<GossipContainer.GossipEntry> unpack(UUID $$0) {
         return this.entries.object2IntEntrySet().stream().map($$1 -> new GossipContainer.GossipEntry($$0, (GossipType)$$1.getKey(), $$1.getIntValue()));
      }

      public void decay() {
         ObjectIterator<Entry<GossipType>> $$0 = this.entries.object2IntEntrySet().iterator();

         while ($$0.hasNext()) {
            Entry<GossipType> $$1 = (Entry<GossipType>)$$0.next();
            int $$2 = $$1.getIntValue() - ((GossipType)$$1.getKey()).decayPerDay;
            if ($$2 < 2) {
               $$0.remove();
            } else {
               $$1.setValue($$2);
            }
         }
      }

      public boolean isEmpty() {
         return this.entries.isEmpty();
      }

      public void makeSureValueIsntTooLowOrTooHigh(GossipType $$0) {
         int $$1 = this.entries.getInt($$0);
         if ($$1 > $$0.max) {
            this.entries.put($$0, $$0.max);
         }

         if ($$1 < 2) {
            this.remove($$0);
         }
      }

      public void remove(GossipType $$0) {
         this.entries.removeInt($$0);
      }
   }

   record GossipEntry(UUID target, GossipType type, int value) {
      public static final Codec<GossipContainer.GossipEntry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipContainer.GossipEntry::target),
               GossipType.CODEC.fieldOf("Type").forGetter(GossipContainer.GossipEntry::type),
               ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipContainer.GossipEntry::value)
            )
            .apply($$0, GossipContainer.GossipEntry::new)
      );

      public int weightedValue() {
         return this.value * this.type.weight;
      }
   }
}
