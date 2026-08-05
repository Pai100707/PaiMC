package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
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
import net.minecraft.world.entity.player.Player;

public class AngerManagement {
   @VisibleForTesting
   protected static final int CONVERSION_DELAY = 2;
   @VisibleForTesting
   protected static final int MAX_ANGER = 150;
   private static final int DEFAULT_ANGER_DECREASE = 1;
   private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
   int highestAnger;
   private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create(
      $$0 -> $$0.group(UUIDUtil.CODEC.fieldOf("uuid").forGetter(Pair::getFirst), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond))
         .apply($$0, Pair::of)
   );
   private final Predicate<net.minecraft.world.entity.Entity> filter;
   @VisibleForTesting
   protected final ArrayList<net.minecraft.world.entity.Entity> suspects;
   private final AngerManagement.Sorter suspectSorter;
   @VisibleForTesting
   protected final Object2IntMap<net.minecraft.world.entity.Entity> angerBySuspect;
   @VisibleForTesting
   protected final Object2IntMap<UUID> angerByUuid;

   public static Codec<AngerManagement> codec(Predicate<net.minecraft.world.entity.Entity> $$0) {
      return RecordCodecBuilder.create(
         $$1 -> $$1.group(SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs))
            .apply($$1, $$1x -> new AngerManagement($$0, $$1x))
      );
   }

   public AngerManagement(Predicate<net.minecraft.world.entity.Entity> $$0, List<Pair<UUID, Integer>> $$1) {
      this.filter = $$0;
      this.suspects = new ArrayList<>();
      this.suspectSorter = new AngerManagement.Sorter(this);
      this.angerBySuspect = new Object2IntOpenHashMap();
      this.angerByUuid = new Object2IntOpenHashMap($$1.size());
      $$1.forEach($$0x -> this.angerByUuid.put((UUID)$$0x.getFirst(), (Integer)$$0x.getSecond()));
   }

   private List<Pair<UUID, Integer>> createUuidAngerPairs() {
      return Streams.concat(
            new Stream[]{
               this.suspects.stream().map($$0 -> Pair.of($$0.getUUID(), this.angerBySuspect.getInt($$0))),
               this.angerByUuid.object2IntEntrySet().stream().map($$0 -> Pair.of((UUID)$$0.getKey(), $$0.getIntValue()))
            }
         )
         .collect(Collectors.toList());
   }

   public void tick(ServerLevel $$0, Predicate<net.minecraft.world.entity.Entity> $$1) {
      this.conversionDelay--;
      if (this.conversionDelay <= 0) {
         this.convertFromUuids($$0);
         this.conversionDelay = 2;
      }

      ObjectIterator<Entry<UUID>> $$2 = this.angerByUuid.object2IntEntrySet().iterator();

      while ($$2.hasNext()) {
         Entry<UUID> $$3 = (Entry<UUID>)$$2.next();
         int $$4 = $$3.getIntValue();
         if ($$4 <= 1) {
            $$2.remove();
         } else {
            $$3.setValue($$4 - 1);
         }
      }

      ObjectIterator<Entry<net.minecraft.world.entity.Entity>> $$5 = this.angerBySuspect.object2IntEntrySet().iterator();

      while ($$5.hasNext()) {
         Entry<net.minecraft.world.entity.Entity> $$6 = (Entry<net.minecraft.world.entity.Entity>)$$5.next();
         int $$7 = $$6.getIntValue();
         net.minecraft.world.entity.Entity $$8 = (net.minecraft.world.entity.Entity)$$6.getKey();
         net.minecraft.world.entity.Entity.RemovalReason $$9 = $$8.getRemovalReason();
         if ($$7 > 1 && $$1.test($$8) && $$9 == null) {
            $$6.setValue($$7 - 1);
         } else {
            this.suspects.remove($$8);
            $$5.remove();
            if ($$7 > 1 && $$9 != null) {
               switch ($$9) {
                  case CHANGED_DIMENSION:
                  case UNLOADED_TO_CHUNK:
                  case UNLOADED_WITH_PLAYER:
                     this.angerByUuid.put($$8.getUUID(), $$7 - 1);
               }
            }
         }
      }

      this.sortAndUpdateHighestAnger();
   }

   private void sortAndUpdateHighestAnger() {
      this.highestAnger = 0;
      this.suspects.sort(this.suspectSorter);
      if (this.suspects.size() == 1) {
         this.highestAnger = this.angerBySuspect.getInt(this.suspects.get(0));
      }
   }

   private void convertFromUuids(ServerLevel $$0) {
      ObjectIterator<Entry<UUID>> $$1 = this.angerByUuid.object2IntEntrySet().iterator();

      while ($$1.hasNext()) {
         Entry<UUID> $$2 = (Entry<UUID>)$$1.next();
         int $$3 = $$2.getIntValue();
         net.minecraft.world.entity.Entity $$4 = $$0.getEntity((UUID)$$2.getKey());
         if ($$4 != null) {
            this.angerBySuspect.put($$4, $$3);
            this.suspects.add($$4);
            $$1.remove();
         }
      }
   }

   public int increaseAnger(net.minecraft.world.entity.Entity $$0, int $$1) {
      boolean $$2 = !this.angerBySuspect.containsKey($$0);
      int $$3 = this.angerBySuspect.computeInt($$0, ($$1x, $$2x) -> Math.min(150, ($$2x == null ? 0 : $$2x) + $$1));
      if ($$2) {
         int $$4 = this.angerByUuid.removeInt($$0.getUUID());
         $$3 += $$4;
         this.angerBySuspect.put($$0, $$3);
         this.suspects.add($$0);
      }

      this.sortAndUpdateHighestAnger();
      return $$3;
   }

   public void clearAnger(net.minecraft.world.entity.Entity $$0) {
      this.angerBySuspect.removeInt($$0);
      this.suspects.remove($$0);
      this.sortAndUpdateHighestAnger();
   }

   
   private net.minecraft.world.entity.Entity getTopSuspect() {
      return this.suspects.stream().filter(this.filter).findFirst().orElse(null);
   }

   public int getActiveAnger(net.minecraft.world.entity.Entity $$0) {
      return $$0 == null ? this.highestAnger : this.angerBySuspect.getInt($$0);
   }

   public Optional<net.minecraft.world.entity.LivingEntity> getActiveEntity() {
      return Optional.ofNullable(this.getTopSuspect())
         .filter($$0 -> $$0 instanceof net.minecraft.world.entity.LivingEntity)
         .map($$0 -> (net.minecraft.world.entity.LivingEntity)$$0);
   }

   @VisibleForTesting
   protected record Sorter(AngerManagement angerManagement) implements Comparator<net.minecraft.world.entity.Entity> {
      public int compare(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Entity $$1) {
         if ($$0.equals($$1)) {
            return 0;
         } else {
            int $$2 = this.angerManagement.angerBySuspect.getOrDefault($$0, 0);
            int $$3 = this.angerManagement.angerBySuspect.getOrDefault($$1, 0);
            this.angerManagement.highestAnger = Math.max(this.angerManagement.highestAnger, Math.max($$2, $$3));
            boolean $$4 = AngerLevel.byAnger($$2).isAngry();
            boolean $$5 = AngerLevel.byAnger($$3).isAngry();
            if ($$4 != $$5) {
               return $$4 ? -1 : 1;
            } else {
               boolean $$6 = $$0 instanceof Player;
               boolean $$7 = $$1 instanceof Player;
               if ($$6 != $$7) {
                  return $$6 ? -1 : 1;
               } else {
                  return Integer.compare($$3, $$2);
               }
            }
         }
      }
   }
}
