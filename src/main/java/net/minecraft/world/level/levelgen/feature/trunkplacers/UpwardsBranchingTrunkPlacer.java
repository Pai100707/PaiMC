package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> trunkPlacerParts($$0)
         .and(
            $$0.group(
               IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter($$0x -> $$0x.extraBranchSteps),
               Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter($$0x -> $$0x.placeBranchPerLogProbability),
               IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter($$0x -> $$0x.extraBranchLength),
               RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter($$0x -> $$0x.canGrowThrough)
            )
         )
         .apply($$0, UpwardsBranchingTrunkPlacer::new)
   );
   private final IntProvider extraBranchSteps;
   private final float placeBranchPerLogProbability;
   private final IntProvider extraBranchLength;
   private final HolderSet<Block> canGrowThrough;

   public UpwardsBranchingTrunkPlacer(int $$0, int $$1, int $$2, IntProvider $$3, float $$4, IntProvider $$5, HolderSet<Block> $$6) {
      super($$0, $$1, $$2);
      this.extraBranchSteps = $$3;
      this.placeBranchPerLogProbability = $$4;
      this.extraBranchLength = $$5;
      this.canGrowThrough = $$6;
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      List<FoliagePlacer.FoliageAttachment> $$6 = Lists.newArrayList();
      MutableBlockPos $$7 = new MutableBlockPos();

      for (int $$8 = 0; $$8 < $$3; $$8++) {
         int $$9 = $$4.getY() + $$8;
         if (this.placeLog($$0, $$1, $$2, $$7.set($$4.getX(), $$9, $$4.getZ()), $$5) && $$8 < $$3 - 1 && $$2.nextFloat() < this.placeBranchPerLogProbability) {
            Direction $$10 = Plane.HORIZONTAL.getRandomDirection($$2);
            int $$11 = this.extraBranchLength.sample($$2);
            int $$12 = Math.max(0, $$11 - this.extraBranchLength.sample($$2) - 1);
            int $$13 = this.extraBranchSteps.sample($$2);
            this.placeBranch($$0, $$1, $$2, $$3, $$5, $$6, $$7, $$9, $$10, $$12, $$13);
         }

         if ($$8 == $$3 - 1) {
            $$6.add(new FoliagePlacer.FoliageAttachment($$7.set($$4.getX(), $$9 + 1, $$4.getZ()), 0, false));
         }
      }

      return $$6;
   }

   private void placeBranch(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      int $$3,
      TreeConfiguration $$4,
      List<FoliagePlacer.FoliageAttachment> $$5,
      MutableBlockPos $$6,
      int $$7,
      Direction $$8,
      int $$9,
      int $$10
   ) {
      int $$11 = $$7 + $$9;
      int $$12 = $$6.getX();
      int $$13 = $$6.getZ();
      int $$14 = $$9;

      while ($$14 < $$3 && $$10 > 0) {
         if ($$14 >= 1) {
            int $$15 = $$7 + $$14;
            $$12 += $$8.getStepX();
            $$13 += $$8.getStepZ();
            $$11 = $$15;
            if (this.placeLog($$0, $$1, $$2, $$6.set($$12, $$15, $$13), $$4)) {
               $$11 = $$15 + 1;
            }

            $$5.add(new FoliagePlacer.FoliageAttachment($$6.immutable(), 0, false));
         }

         $$14++;
         $$10--;
      }

      if ($$11 - $$7 > 1) {
         BlockPos $$16 = new BlockPos($$12, $$11, $$13);
         $$5.add(new FoliagePlacer.FoliageAttachment($$16, 0, false));
         $$5.add(new FoliagePlacer.FoliageAttachment($$16.below(2), 0, false));
      }
   }

   @Override
   protected boolean validTreePos(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return super.validTreePos($$0, $$1) || $$0.isStateAtPosition($$1, $$0x -> $$0x.is(this.canGrowThrough));
   }
}
