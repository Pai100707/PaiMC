package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.debug.DebugPoiInfo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;

public class PoiManager extends SectionStorage<PoiSection, PoiSection.Packed> {
   public static final int MAX_VILLAGE_DISTANCE = 6;
   public static final int VILLAGE_SECTION_SIZE = 1;
   private final PoiManager.DistanceTracker distanceTracker;
   private final LongSet loadedChunks = new LongOpenHashSet();

   public PoiManager(RegionStorageInfo $$0, Path $$1, DataFixer $$2, boolean $$3, RegistryAccess $$4, ChunkIOErrorReporter $$5, LevelHeightAccessor $$6) {
      super(
         new SimpleRegionStorage($$0, $$1, $$2, $$3, DataFixTypes.POI_CHUNK),
         PoiSection.Packed.CODEC,
         PoiSection::pack,
         PoiSection.Packed::unpack,
         PoiSection::new,
         $$4,
         $$5,
         $$6
      );
      this.distanceTracker = new PoiManager.DistanceTracker();
   }

   
   public PoiRecord add(BlockPos $$0, Holder<PoiType> $$1) {
      return ((PoiSection)this.getOrCreate(SectionPos.asLong($$0))).add($$0, $$1);
   }

   public void remove(BlockPos $$0) {
      this.getOrLoad(SectionPos.asLong($$0)).ifPresent($$1 -> $$1.remove($$0));
   }

   public long getCountInRange(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, PoiManager.Occupancy $$3) {
      return this.getInRange($$0, $$1, $$2, $$3).count();
   }

   public boolean existsAtPosition(ResourceKey<PoiType> $$0, BlockPos $$1) {
      return this.exists($$1, $$1x -> $$1x.is($$0));
   }

   public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, PoiManager.Occupancy $$3) {
      int $$4 = Math.floorDiv($$2, 16) + 1;
      return ChunkPos.rangeClosed(new ChunkPos($$1), $$4).flatMap($$2x -> this.getInChunk($$0, $$2x, $$3)).filter($$2x -> {
         BlockPos $$3x = $$2x.getPos();
         return Math.abs($$3x.getX() - $$1.getX()) <= $$2 && Math.abs($$3x.getZ() - $$1.getZ()) <= $$2;
      });
   }

   public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, PoiManager.Occupancy $$3) {
      int $$4 = $$2 * $$2;
      return this.getInSquare($$0, $$1, $$2, $$3).filter($$2x -> $$2x.getPos().distSqr($$1) <= $$4);
   }

   @VisibleForDebug
   public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> $$0, ChunkPos $$1, PoiManager.Occupancy $$2) {
      return IntStream.rangeClosed(this.levelHeightAccessor.getMinSectionY(), this.levelHeightAccessor.getMaxSectionY())
         .boxed()
         .map($$1x -> this.getOrLoad(SectionPos.of($$1, $$1x).asLong()))
         .filter(Optional::isPresent)
         .flatMap($$2x -> ((PoiSection)$$2x.get()).getRecords($$0, $$2));
   }

   public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, PoiManager.Occupancy $$4) {
      return this.getInRange($$0, $$2, $$3, $$4).map(PoiRecord::getPos).filter($$1);
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(
      Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, PoiManager.Occupancy $$4
   ) {
      return this.getInRange($$0, $$2, $$3, $$4).filter($$1x -> $$1.test($$1x.getPos())).map($$0x -> Pair.of($$0x.getPoiType(), $$0x.getPos()));
   }

   public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(
      Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, PoiManager.Occupancy $$4
   ) {
      return this.findAllWithType($$0, $$1, $$2, $$3, $$4).sorted(Comparator.comparingDouble($$1x -> ((BlockPos)$$1x.getSecond()).distSqr($$2)));
   }

   public Optional<BlockPos> find(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, PoiManager.Occupancy $$4) {
      return this.findAll($$0, $$1, $$2, $$3, $$4).findFirst();
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, PoiManager.Occupancy $$3) {
      return this.getInRange($$0, $$1, $$2, $$3).map(PoiRecord::getPos).min(Comparator.comparingDouble($$1x -> $$1x.distSqr($$1)));
   }

   public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> $$0, BlockPos $$1, int $$2, PoiManager.Occupancy $$3) {
      return this.getInRange($$0, $$1, $$2, $$3)
         .min(Comparator.comparingDouble($$1x -> $$1x.getPos().distSqr($$1)))
         .map($$0x -> Pair.of($$0x.getPoiType(), $$0x.getPos()));
   }

   public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, BlockPos $$2, int $$3, PoiManager.Occupancy $$4) {
      return this.getInRange($$0, $$2, $$3, $$4).map(PoiRecord::getPos).filter($$1).min(Comparator.comparingDouble($$1x -> $$1x.distSqr($$2)));
   }

   public Optional<BlockPos> take(Predicate<Holder<PoiType>> $$0, BiPredicate<Holder<PoiType>, BlockPos> $$1, BlockPos $$2, int $$3) {
      return this.getInRange($$0, $$2, $$3, PoiManager.Occupancy.HAS_SPACE)
         .filter($$1x -> $$1.test($$1x.getPoiType(), $$1x.getPos()))
         .findFirst()
         .map($$0x -> {
            $$0x.acquireTicket();
            return $$0x.getPos();
         });
   }

   public Optional<BlockPos> getRandom(
      Predicate<Holder<PoiType>> $$0, Predicate<BlockPos> $$1, PoiManager.Occupancy $$2, BlockPos $$3, int $$4, RandomSource $$5
   ) {
      List<PoiRecord> $$6 = Util.toShuffledList(this.getInRange($$0, $$3, $$4, $$2), $$5);
      return $$6.stream().filter($$1x -> $$1.test($$1x.getPos())).findFirst().map(PoiRecord::getPos);
   }

   public boolean release(BlockPos $$0) {
      return this.getOrLoad(SectionPos.asLong($$0))
         .map($$1 -> $$1.release($$0))
         .orElseThrow(() -> (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + $$0)));
   }

   public boolean exists(BlockPos $$0, Predicate<Holder<PoiType>> $$1) {
      return this.getOrLoad(SectionPos.asLong($$0)).map($$2 -> $$2.exists($$0, $$1)).orElse(false);
   }

   public Optional<Holder<PoiType>> getType(BlockPos $$0) {
      return this.getOrLoad(SectionPos.asLong($$0)).flatMap($$1 -> $$1.getType($$0));
   }

   @VisibleForDebug
   
   public DebugPoiInfo getDebugPoiInfo(BlockPos $$0) {
      return this.getOrLoad(SectionPos.asLong($$0)).flatMap($$1 -> $$1.getDebugPoiInfo($$0)).orElse(null);
   }

   public int sectionsToVillage(SectionPos $$0) {
      this.distanceTracker.runAllUpdates();
      return this.distanceTracker.getLevel($$0.asLong());
   }

   boolean isVillageCenter(long $$0) {
      Optional<PoiSection> $$1 = this.get($$0);
      return $$1 == null
         ? false
         : $$1.<Boolean>map($$0x -> $$0x.getRecords($$0xx -> $$0xx.is(PoiTypeTags.VILLAGE), PoiManager.Occupancy.IS_OCCUPIED).findAny().isPresent())
            .orElse(false);
   }

   public void tick(BooleanSupplier $$0) {
      super.tick($$0);
      this.distanceTracker.runAllUpdates();
   }

   protected void setDirty(long $$0) {
      super.setDirty($$0);
      this.distanceTracker.update($$0, this.distanceTracker.getLevelFromSource($$0), false);
   }

   protected void onSectionLoad(long $$0) {
      this.distanceTracker.update($$0, this.distanceTracker.getLevelFromSource($$0), false);
   }

   public void checkConsistencyWithBlocks(SectionPos $$0, LevelChunkSection $$1) {
      Util.ifElse(this.getOrLoad($$0.asLong()), $$2 -> $$2.refresh($$2x -> {
         if (mayHavePoi($$1)) {
            this.updateFromSection($$1, $$0, $$2x);
         }
      }), () -> {
         if (mayHavePoi($$1)) {
            PoiSection $$2 = (PoiSection)this.getOrCreate($$0.asLong());
            this.updateFromSection($$1, $$0, $$2::add);
         }
      });
   }

   private static boolean mayHavePoi(LevelChunkSection $$0) {
      return $$0.maybeHas(PoiTypes::hasPoi);
   }

   private void updateFromSection(LevelChunkSection $$0, SectionPos $$1, BiConsumer<BlockPos, Holder<PoiType>> $$2) {
      $$1.blocksInside()
         .forEach(
            $$2x -> {
               BlockState $$3 = $$0.getBlockState(
                  SectionPos.sectionRelative($$2x.getX()), SectionPos.sectionRelative($$2x.getY()), SectionPos.sectionRelative($$2x.getZ())
               );
               PoiTypes.forState($$3).ifPresent($$2xx -> $$2.accept($$2x, $$2xx));
            }
         );
   }

   public void ensureLoadedAndValid(LevelReader $$0, BlockPos $$1, int $$2) {
      SectionPos.aroundChunk(new ChunkPos($$1), Math.floorDiv($$2, 16), this.levelHeightAccessor.getMinSectionY(), this.levelHeightAccessor.getMaxSectionY())
         .map($$0x -> Pair.of($$0x, this.getOrLoad($$0x.asLong())))
         .filter($$0x -> !((Optional)$$0x.getSecond()).map(PoiSection::isValid).orElse(false))
         .map($$0x -> ((SectionPos)$$0x.getFirst()).chunk())
         .filter($$0x -> this.loadedChunks.add($$0x.toLong()))
         .forEach($$1x -> $$0.getChunk($$1x.x, $$1x.z, ChunkStatus.EMPTY));
   }

   final class DistanceTracker extends SectionTracker {
      private final Long2ByteMap levels = new Long2ByteOpenHashMap();

      protected DistanceTracker() {
         super(7, 16, 256);
         this.levels.defaultReturnValue((byte)7);
      }

      protected int getLevelFromSource(long $$0) {
         return PoiManager.this.isVillageCenter($$0) ? 0 : 7;
      }

      protected int getLevel(long $$0) {
         return this.levels.get($$0);
      }

      protected void setLevel(long $$0, int $$1) {
         if ($$1 > 6) {
            this.levels.remove($$0);
         } else {
            this.levels.put($$0, (byte)$$1);
         }
      }

      public void runAllUpdates() {
         super.runUpdates(Integer.MAX_VALUE);
      }
   }

   public static enum Occupancy {
      HAS_SPACE(PoiRecord::hasSpace),
      IS_OCCUPIED(PoiRecord::isOccupied),
      ANY($$0 -> true);

      private final Predicate<? super PoiRecord> test;

      private Occupancy(final Predicate<? super PoiRecord> $$0) {
         this.test = $$0;
      }

      public Predicate<? super PoiRecord> getTest() {
         return this.test;
      }
   }
}
