package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
   public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
   protected final IntProvider radius;
   protected final IntProvider offset;

   protected static <P extends FoliagePlacer> P2<Mu<P>, IntProvider, IntProvider> foliagePlacerParts(Instance<P> $$0) {
      return $$0.group(
         IntProvider.codec(0, 16).fieldOf("radius").forGetter($$0x -> $$0x.radius), IntProvider.codec(0, 16).fieldOf("offset").forGetter($$0x -> $$0x.offset)
      );
   }

   public FoliagePlacer(IntProvider $$0, IntProvider $$1) {
      this.radius = $$0;
      this.offset = $$1;
   }

   protected abstract FoliagePlacerType<?> type();

   public void createFoliage(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      FoliagePlacer.FoliageSetter $$1,
      RandomSource $$2,
      TreeConfiguration $$3,
      int $$4,
      FoliagePlacer.FoliageAttachment $$5,
      int $$6,
      int $$7
   ) {
      this.createFoliage($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7, this.offset($$2));
   }

   protected abstract void createFoliage(
      net.minecraft.world.level.LevelSimulatedReader var1,
      FoliagePlacer.FoliageSetter var2,
      RandomSource var3,
      TreeConfiguration var4,
      int var5,
      FoliagePlacer.FoliageAttachment var6,
      int var7,
      int var8,
      int var9
   );

   public abstract int foliageHeight(RandomSource var1, int var2, TreeConfiguration var3);

   public int foliageRadius(RandomSource $$0, int $$1) {
      return this.radius.sample($$0);
   }

   private int offset(RandomSource $$0) {
      return this.offset.sample($$0);
   }

   protected abstract boolean shouldSkipLocation(RandomSource var1, int var2, int var3, int var4, int var5, boolean var6);

   protected boolean shouldSkipLocationSigned(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      int $$6;
      int $$7;
      if ($$5) {
         $$6 = Math.min(Math.abs($$1), Math.abs($$1 - 1));
         $$7 = Math.min(Math.abs($$3), Math.abs($$3 - 1));
      } else {
         $$6 = Math.abs($$1);
         $$7 = Math.abs($$3);
      }

      return this.shouldSkipLocation($$0, $$6, $$2, $$7, $$4, $$5);
   }

   protected void placeLeavesRow(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      FoliagePlacer.FoliageSetter $$1,
      RandomSource $$2,
      TreeConfiguration $$3,
      BlockPos $$4,
      int $$5,
      int $$6,
      boolean $$7
   ) {
      int $$8 = $$7 ? 1 : 0;
      MutableBlockPos $$9 = new MutableBlockPos();

      for (int $$10 = -$$5; $$10 <= $$5 + $$8; $$10++) {
         for (int $$11 = -$$5; $$11 <= $$5 + $$8; $$11++) {
            if (!this.shouldSkipLocationSigned($$2, $$10, $$6, $$11, $$5, $$7)) {
               $$9.setWithOffset($$4, $$10, $$6, $$11);
               tryPlaceLeaf($$0, $$1, $$2, $$3, $$9);
            }
         }
      }
   }

   protected final void placeLeavesRowWithHangingLeavesBelow(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      FoliagePlacer.FoliageSetter $$1,
      RandomSource $$2,
      TreeConfiguration $$3,
      BlockPos $$4,
      int $$5,
      int $$6,
      boolean $$7,
      float $$8,
      float $$9
   ) {
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      int $$10 = $$7 ? 1 : 0;
      BlockPos $$11 = $$4.below();
      MutableBlockPos $$12 = new MutableBlockPos();

      for (Direction $$13 : Plane.HORIZONTAL) {
         Direction $$14 = $$13.getClockWise();
         int $$15 = $$14.getAxisDirection() == AxisDirection.POSITIVE ? $$5 + $$10 : $$5;
         $$12.setWithOffset($$4, 0, $$6 - 1, 0).move($$14, $$15).move($$13, -$$5);
         int $$16 = -$$5;

         while ($$16 < $$5 + $$10) {
            boolean $$17 = $$1.isSet($$12.move(Direction.UP));
            $$12.move(Direction.DOWN);
            if ($$17 && tryPlaceExtension($$0, $$1, $$2, $$3, $$8, $$11, $$12)) {
               $$12.move(Direction.DOWN);
               tryPlaceExtension($$0, $$1, $$2, $$3, $$9, $$11, $$12);
               $$12.move(Direction.UP);
            }

            $$16++;
            $$12.move($$13);
         }
      }
   }

   private static boolean tryPlaceExtension(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      FoliagePlacer.FoliageSetter $$1,
      RandomSource $$2,
      TreeConfiguration $$3,
      float $$4,
      BlockPos $$5,
      MutableBlockPos $$6
   ) {
      if ($$6.distManhattan($$5) >= 7) {
         return false;
      } else {
         return $$2.nextFloat() > $$4 ? false : tryPlaceLeaf($$0, $$1, $$2, $$3, $$6);
      }
   }

   protected static boolean tryPlaceLeaf(
      net.minecraft.world.level.LevelSimulatedReader $$0, FoliagePlacer.FoliageSetter $$1, RandomSource $$2, TreeConfiguration $$3, BlockPos $$4
   ) {
      boolean $$5 = $$0.isStateAtPosition($$4, $$0x -> $$0x.getValueOrElse(BlockStateProperties.PERSISTENT, false));
      if (!$$5 && TreeFeature.validTreePos($$0, $$4)) {
         BlockState $$6 = $$3.foliageProvider.getState($$2, $$4);
         if ($$6.hasProperty(BlockStateProperties.WATERLOGGED)) {
            $$6 = $$6.setValue(BlockStateProperties.WATERLOGGED, $$0.isFluidAtPosition($$4, $$0x -> $$0x.isSourceOfType(Fluids.WATER)));
         }

         $$1.set($$4, $$6);
         return true;
      } else {
         return false;
      }
   }

   public static final class FoliageAttachment {
      private final BlockPos pos;
      private final int radiusOffset;
      private final boolean doubleTrunk;

      public FoliageAttachment(BlockPos $$0, int $$1, boolean $$2) {
         this.pos = $$0;
         this.radiusOffset = $$1;
         this.doubleTrunk = $$2;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int radiusOffset() {
         return this.radiusOffset;
      }

      public boolean doubleTrunk() {
         return this.doubleTrunk;
      }
   }

   public interface FoliageSetter {
      void set(BlockPos var1, BlockState var2);

      boolean isSet(BlockPos var1);
   }
}
