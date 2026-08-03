package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
   public static final int NUMBER_OF_SPIKES = 10;
   private static final int SPIKE_DISTANCE = 42;
   private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder()
      .expireAfterWrite(5L, TimeUnit.MINUTES)
      .build(new SpikeFeature.SpikeCacheLoader());

   public SpikeFeature(Codec<SpikeConfiguration> $$0) {
      super($$0);
   }

   public static List<SpikeFeature.EndSpike> getSpikesForLevel(net.minecraft.world.level.WorldGenLevel $$0) {
      RandomSource $$1 = RandomSource.create($$0.getSeed());
      long $$2 = $$1.nextLong() & 65535L;
      return (List<SpikeFeature.EndSpike>)SPIKE_CACHE.getUnchecked($$2);
   }

   @Override
   public boolean place(FeaturePlaceContext<SpikeConfiguration> $$0) {
      SpikeConfiguration $$1 = $$0.config();
      net.minecraft.world.level.WorldGenLevel $$2 = $$0.level();
      RandomSource $$3 = $$0.random();
      BlockPos $$4 = $$0.origin();
      List<SpikeFeature.EndSpike> $$5 = $$1.getSpikes();
      if ($$5.isEmpty()) {
         $$5 = getSpikesForLevel($$2);
      }

      for (SpikeFeature.EndSpike $$6 : $$5) {
         if ($$6.isCenterWithinChunk($$4)) {
            this.placeSpike($$2, $$3, $$1, $$6);
         }
      }

      return true;
   }

   private void placeSpike(net.minecraft.world.level.ServerLevelAccessor $$0, RandomSource $$1, SpikeConfiguration $$2, SpikeFeature.EndSpike $$3) {
      int $$4 = $$3.getRadius();

      for (BlockPos $$5 : BlockPos.betweenClosed(
         new BlockPos($$3.getCenterX() - $$4, $$0.getMinY(), $$3.getCenterZ() - $$4),
         new BlockPos($$3.getCenterX() + $$4, $$3.getHeight() + 10, $$3.getCenterZ() + $$4)
      )) {
         if ($$5.distToLowCornerSqr($$3.getCenterX(), $$5.getY(), $$3.getCenterZ()) <= $$4 * $$4 + 1 && $$5.getY() < $$3.getHeight()) {
            this.setBlock($$0, $$5, Blocks.OBSIDIAN.defaultBlockState());
         } else if ($$5.getY() > 65) {
            this.setBlock($$0, $$5, Blocks.AIR.defaultBlockState());
         }
      }

      if ($$3.isGuarded()) {
         int $$6 = -2;
         int $$7 = 2;
         int $$8 = 3;
         MutableBlockPos $$9 = new MutableBlockPos();

         for (int $$10 = -2; $$10 <= 2; $$10++) {
            for (int $$11 = -2; $$11 <= 2; $$11++) {
               for (int $$12 = 0; $$12 <= 3; $$12++) {
                  boolean $$13 = Mth.abs($$10) == 2;
                  boolean $$14 = Mth.abs($$11) == 2;
                  boolean $$15 = $$12 == 3;
                  if ($$13 || $$14 || $$15) {
                     boolean $$16 = $$10 == -2 || $$10 == 2 || $$15;
                     boolean $$17 = $$11 == -2 || $$11 == 2 || $$15;
                     BlockState $$18 = Blocks.IRON_BARS
                        .defaultBlockState()
                        .setValue(IronBarsBlock.NORTH, $$16 && $$11 != -2)
                        .setValue(IronBarsBlock.SOUTH, $$16 && $$11 != 2)
                        .setValue(IronBarsBlock.WEST, $$17 && $$10 != -2)
                        .setValue(IronBarsBlock.EAST, $$17 && $$10 != 2);
                     this.setBlock($$0, $$9.set($$3.getCenterX() + $$10, $$3.getHeight() + $$12, $$3.getCenterZ() + $$11), $$18);
                  }
               }
            }
         }
      }

      EndCrystal $$19 = (EndCrystal)EntityType.END_CRYSTAL.create($$0.getLevel(), EntitySpawnReason.STRUCTURE);
      if ($$19 != null) {
         $$19.setBeamTarget($$2.getCrystalBeamTarget());
         $$19.setInvulnerable($$2.isCrystalInvulnerable());
         $$19.snapTo($$3.getCenterX() + 0.5, $$3.getHeight() + 1, $$3.getCenterZ() + 0.5, $$1.nextFloat() * 360.0F, 0.0F);
         $$0.addFreshEntity($$19);
         BlockPos $$20 = $$19.blockPosition();
         this.setBlock($$0, $$20.below(), Blocks.BEDROCK.defaultBlockState());
         this.setBlock($$0, $$20, FireBlock.getState($$0, $$20));
      }
   }

   public static class EndSpike {
      public static final Codec<SpikeFeature.EndSpike> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.INT.fieldOf("centerX").orElse(0).forGetter($$0x -> $$0x.centerX),
               Codec.INT.fieldOf("centerZ").orElse(0).forGetter($$0x -> $$0x.centerZ),
               Codec.INT.fieldOf("radius").orElse(0).forGetter($$0x -> $$0x.radius),
               Codec.INT.fieldOf("height").orElse(0).forGetter($$0x -> $$0x.height),
               Codec.BOOL.fieldOf("guarded").orElse(false).forGetter($$0x -> $$0x.guarded)
            )
            .apply($$0, SpikeFeature.EndSpike::new)
      );
      private final int centerX;
      private final int centerZ;
      private final int radius;
      private final int height;
      private final boolean guarded;
      private final AABB topBoundingBox;

      public EndSpike(int $$0, int $$1, int $$2, int $$3, boolean $$4) {
         this.centerX = $$0;
         this.centerZ = $$1;
         this.radius = $$2;
         this.height = $$3;
         this.guarded = $$4;
         this.topBoundingBox = new AABB($$0 - $$2, DimensionType.MIN_Y, $$1 - $$2, $$0 + $$2, DimensionType.MAX_Y, $$1 + $$2);
      }

      public boolean isCenterWithinChunk(BlockPos $$0) {
         return SectionPos.blockToSectionCoord($$0.getX()) == SectionPos.blockToSectionCoord(this.centerX)
            && SectionPos.blockToSectionCoord($$0.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
      }

      public int getCenterX() {
         return this.centerX;
      }

      public int getCenterZ() {
         return this.centerZ;
      }

      public int getRadius() {
         return this.radius;
      }

      public int getHeight() {
         return this.height;
      }

      public boolean isGuarded() {
         return this.guarded;
      }

      public AABB getTopBoundingBox() {
         return this.topBoundingBox;
      }
   }

   static class SpikeCacheLoader extends CacheLoader<Long, List<SpikeFeature.EndSpike>> {
      public List<SpikeFeature.EndSpike> load(Long $$0) {
         IntArrayList $$1 = Util.toShuffledList(IntStream.range(0, 10), RandomSource.create($$0));
         List<SpikeFeature.EndSpike> $$2 = Lists.newArrayList();

         for (int $$3 = 0; $$3 < 10; $$3++) {
            int $$4 = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + (Math.PI / 10) * $$3)));
            int $$5 = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + (Math.PI / 10) * $$3)));
            int $$6 = $$1.get($$3);
            int $$7 = 2 + $$6 / 3;
            int $$8 = 76 + $$6 * 3;
            boolean $$9 = $$6 == 1 || $$6 == 2;
            $$2.add(new SpikeFeature.EndSpike($$4, $$5, $$7, $$8, $$9));
         }

         return $$2;
      }
   }
}
