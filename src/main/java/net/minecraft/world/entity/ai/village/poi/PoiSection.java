package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.debug.DebugPoiInfo;
import org.slf4j.Logger;

public class PoiSection {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap();
   private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.newHashMap();
   private final Runnable setDirty;
   private boolean isValid;

   public PoiSection(Runnable $$0) {
      this($$0, true, ImmutableList.of());
   }

   PoiSection(Runnable $$0, boolean $$1, List<PoiRecord> $$2) {
      this.setDirty = $$0;
      this.isValid = $$1;
      $$2.forEach(this::add);
   }

   public PoiSection.Packed pack() {
      return new PoiSection.Packed(this.isValid, this.records.values().stream().map(PoiRecord::pack).toList());
   }

   public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> $$0, PoiManager.Occupancy $$1) {
      return this.byType
         .entrySet()
         .stream()
         .filter($$1x -> $$0.test((Holder<PoiType>)$$1x.getKey()))
         .flatMap($$0x -> ((Set)$$0x.getValue()).stream())
         .filter($$1.getTest());
   }

   
   public PoiRecord add(BlockPos $$0, Holder<PoiType> $$1) {
      PoiRecord $$2 = new PoiRecord($$0, $$1, this.setDirty);
      if (this.add($$2)) {
         LOGGER.debug("Added POI of type {} @ {}", $$1.getRegisteredName(), $$0);
         this.setDirty.run();
         return $$2;
      } else {
         return null;
      }
   }

   private boolean add(PoiRecord $$0) {
      BlockPos $$1 = $$0.getPos();
      Holder<PoiType> $$2 = $$0.getPoiType();
      short $$3 = SectionPos.sectionRelativePos($$1);
      PoiRecord $$4 = (PoiRecord)this.records.get($$3);
      if ($$4 != null) {
         if ($$2.equals($$4.getPoiType())) {
            return false;
         }

         Util.logAndPauseIfInIde("POI data mismatch: already registered at " + $$1);
      }

      this.records.put($$3, $$0);
      this.byType.computeIfAbsent($$2, $$0x -> Sets.newHashSet()).add($$0);
      return true;
   }

   public void remove(BlockPos $$0) {
      PoiRecord $$1 = (PoiRecord)this.records.remove(SectionPos.sectionRelativePos($$0));
      if ($$1 == null) {
         LOGGER.error("POI data mismatch: never registered at {}", $$0);
      } else {
         this.byType.get($$1.getPoiType()).remove($$1);
         LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer($$1::getPoiType), LogUtils.defer($$1::getPos));
         this.setDirty.run();
      }
   }

   @Deprecated
   @VisibleForDebug
   public int getFreeTickets(BlockPos $$0) {
      return this.getPoiRecord($$0).map(PoiRecord::getFreeTickets).orElse(0);
   }

   public boolean release(BlockPos $$0) {
      PoiRecord $$1 = (PoiRecord)this.records.get(SectionPos.sectionRelativePos($$0));
      if ($$1 == null) {
         throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + $$0));
      } else {
         boolean $$2 = $$1.releaseTicket();
         this.setDirty.run();
         return $$2;
      }
   }

   public boolean exists(BlockPos $$0, Predicate<Holder<PoiType>> $$1) {
      return this.getType($$0).filter($$1).isPresent();
   }

   public Optional<Holder<PoiType>> getType(BlockPos $$0) {
      return this.getPoiRecord($$0).map(PoiRecord::getPoiType);
   }

   private Optional<PoiRecord> getPoiRecord(BlockPos $$0) {
      return Optional.ofNullable((PoiRecord)this.records.get(SectionPos.sectionRelativePos($$0)));
   }

   public Optional<DebugPoiInfo> getDebugPoiInfo(BlockPos $$0) {
      return this.getPoiRecord($$0).map(DebugPoiInfo::new);
   }

   public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> $$0) {
      if (!this.isValid) {
         Short2ObjectMap<PoiRecord> $$1 = new Short2ObjectOpenHashMap(this.records);
         this.clear();
         $$0.accept(($$1x, $$2) -> {
            short $$3 = SectionPos.sectionRelativePos($$1x);
            PoiRecord $$4 = (PoiRecord)$$1.computeIfAbsent($$3, $$2x -> new PoiRecord($$1x, $$2, this.setDirty));
            this.add($$4);
         });
         this.isValid = true;
         this.setDirty.run();
      }
   }

   private void clear() {
      this.records.clear();
      this.byType.clear();
   }

   boolean isValid() {
      return this.isValid;
   }

   public record Packed(boolean isValid, List<PoiRecord.Packed> records) {
      public static final Codec<PoiSection.Packed> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.BOOL.lenientOptionalFieldOf("Valid", false).forGetter(PoiSection.Packed::isValid),
               PoiRecord.Packed.CODEC.listOf().fieldOf("Records").forGetter(PoiSection.Packed::records)
            )
            .apply($$0, PoiSection.Packed::new)
      );

      public PoiSection unpack(Runnable $$0) {
         return new PoiSection($$0, this.isValid, this.records.stream().map($$1 -> $$1.unpack($$0)).toList());
      }
   }
}
