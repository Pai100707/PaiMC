package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> trunkPlacerParts($$0).apply($$0, ForkingTrunkPlacer::new));

   public ForkingTrunkPlacer(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.FORKING_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      setDirtAt($$0, $$1, $$2, $$4.below(), $$5);
      List<FoliagePlacer.FoliageAttachment> $$6 = Lists.newArrayList();
      Direction $$7 = Plane.HORIZONTAL.getRandomDirection($$2);
      int $$8 = $$3 - $$2.nextInt(4) - 1;
      int $$9 = 3 - $$2.nextInt(3);
      MutableBlockPos $$10 = new MutableBlockPos();
      int $$11 = $$4.getX();
      int $$12 = $$4.getZ();
      OptionalInt $$13 = OptionalInt.empty();

      for (int $$14 = 0; $$14 < $$3; $$14++) {
         int $$15 = $$4.getY() + $$14;
         if ($$14 >= $$8 && $$9 > 0) {
            $$11 += $$7.getStepX();
            $$12 += $$7.getStepZ();
            $$9--;
         }

         if (this.placeLog($$0, $$1, $$2, $$10.set($$11, $$15, $$12), $$5)) {
            $$13 = OptionalInt.of($$15 + 1);
         }
      }

      if ($$13.isPresent()) {
         $$6.add(new FoliagePlacer.FoliageAttachment(new BlockPos($$11, $$13.getAsInt(), $$12), 1, false));
      }

      $$11 = $$4.getX();
      $$12 = $$4.getZ();
      Direction $$16 = Plane.HORIZONTAL.getRandomDirection($$2);
      if ($$16 != $$7) {
         int $$17 = $$8 - $$2.nextInt(2) - 1;
         int $$18 = 1 + $$2.nextInt(3);
         $$13 = OptionalInt.empty();

         for (int $$19 = $$17; $$19 < $$3 && $$18 > 0; $$18--) {
            if ($$19 >= 1) {
               int $$20 = $$4.getY() + $$19;
               $$11 += $$16.getStepX();
               $$12 += $$16.getStepZ();
               if (this.placeLog($$0, $$1, $$2, $$10.set($$11, $$20, $$12), $$5)) {
                  $$13 = OptionalInt.of($$20 + 1);
               }
            }

            $$19++;
         }

         if ($$13.isPresent()) {
            $$6.add(new FoliagePlacer.FoliageAttachment(new BlockPos($$11, $$13.getAsInt(), $$12), 0, false));
         }
      }

      return $$6;
   }
}
