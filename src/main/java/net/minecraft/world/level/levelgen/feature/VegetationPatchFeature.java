package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchFeature extends Feature<VegetationPatchConfiguration> {
   public VegetationPatchFeature(Codec<VegetationPatchConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      VegetationPatchConfiguration $$2 = $$0.config();
      RandomSource $$3 = $$0.random();
      BlockPos $$4 = $$0.origin();
      Predicate<BlockState> $$5 = $$1x -> $$1x.is($$2.replaceable);
      int $$6 = $$2.xzRadius.sample($$3) + 1;
      int $$7 = $$2.xzRadius.sample($$3) + 1;
      Set<BlockPos> $$8 = this.placeGroundPatch($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      this.distributeVegetation($$0, $$1, $$2, $$3, $$8, $$6, $$7);
      return !$$8.isEmpty();
   }

   protected Set<BlockPos> placeGroundPatch(
      net.minecraft.world.level.WorldGenLevel $$0,
      VegetationPatchConfiguration $$1,
      RandomSource $$2,
      BlockPos $$3,
      Predicate<BlockState> $$4,
      int $$5,
      int $$6
   ) {
      MutableBlockPos $$7 = $$3.mutable();
      MutableBlockPos $$8 = $$7.mutable();
      Direction $$9 = $$1.surface.getDirection();
      Direction $$10 = $$9.getOpposite();
      Set<BlockPos> $$11 = new HashSet<>();

      for (int $$12 = -$$5; $$12 <= $$5; $$12++) {
         boolean $$13 = $$12 == -$$5 || $$12 == $$5;

         for (int $$14 = -$$6; $$14 <= $$6; $$14++) {
            boolean $$15 = $$14 == -$$6 || $$14 == $$6;
            boolean $$16 = $$13 || $$15;
            boolean $$17 = $$13 && $$15;
            boolean $$18 = $$16 && !$$17;
            if (!$$17 && (!$$18 || $$1.extraEdgeColumnChance != 0.0F && !($$2.nextFloat() > $$1.extraEdgeColumnChance))) {
               $$7.setWithOffset($$3, $$12, 0, $$14);

               for (int $$19 = 0; $$0.isStateAtPosition($$7, BlockBehaviour.BlockStateBase::isAir) && $$19 < $$1.verticalRange; $$19++) {
                  $$7.move($$9);
               }

               for (int var25 = 0; $$0.isStateAtPosition($$7, $$0x -> !$$0x.isAir()) && var25 < $$1.verticalRange; var25++) {
                  $$7.move($$10);
               }

               $$8.setWithOffset($$7, $$1.surface.getDirection());
               BlockState $$20 = $$0.getBlockState($$8);
               if ($$0.isEmptyBlock($$7) && $$20.isFaceSturdy($$0, $$8, $$1.surface.getDirection().getOpposite())) {
                  int $$21 = $$1.depth.sample($$2) + ($$1.extraBottomBlockChance > 0.0F && $$2.nextFloat() < $$1.extraBottomBlockChance ? 1 : 0);
                  BlockPos $$22 = $$8.immutable();
                  boolean $$23 = this.placeGround($$0, $$1, $$4, $$2, $$8, $$21);
                  if ($$23) {
                     $$11.add($$22);
                  }
               }
            }
         }
      }

      return $$11;
   }

   protected void distributeVegetation(
      FeaturePlaceContext<VegetationPatchConfiguration> $$0,
      net.minecraft.world.level.WorldGenLevel $$1,
      VegetationPatchConfiguration $$2,
      RandomSource $$3,
      Set<BlockPos> $$4,
      int $$5,
      int $$6
   ) {
      for (BlockPos $$7 : $$4) {
         if ($$2.vegetationChance > 0.0F && $$3.nextFloat() < $$2.vegetationChance) {
            this.placeVegetation($$1, $$2, $$0.chunkGenerator(), $$3, $$7);
         }
      }
   }

   protected boolean placeVegetation(
      net.minecraft.world.level.WorldGenLevel $$0, VegetationPatchConfiguration $$1, ChunkGenerator $$2, RandomSource $$3, BlockPos $$4
   ) {
      return ((PlacedFeature)$$1.vegetationFeature.value()).place($$0, $$2, $$3, $$4.relative($$1.surface.getDirection().getOpposite()));
   }

   protected boolean placeGround(
      net.minecraft.world.level.WorldGenLevel $$0, VegetationPatchConfiguration $$1, Predicate<BlockState> $$2, RandomSource $$3, MutableBlockPos $$4, int $$5
   ) {
      for (int $$6 = 0; $$6 < $$5; $$6++) {
         BlockState $$7 = $$1.groundState.getState($$3, $$4);
         BlockState $$8 = $$0.getBlockState($$4);
         if (!$$7.is($$8.getBlock())) {
            if (!$$2.test($$8)) {
               return $$6 != 0;
            }

            $$0.setBlock($$4, $$7, 2);
            $$4.move($$1.surface.getDirection());
         }
      }

      return true;
   }
}
