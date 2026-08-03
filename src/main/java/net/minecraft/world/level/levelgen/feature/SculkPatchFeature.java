package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
   public SculkPatchFeature(Codec<SculkPatchConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<SculkPatchConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      if (!this.canSpreadFrom($$1, $$2)) {
         return false;
      } else {
         SculkPatchConfiguration $$3 = $$0.config();
         RandomSource $$4 = $$0.random();
         SculkSpreader $$5 = SculkSpreader.createWorldGenSpreader();
         int $$6 = $$3.spreadRounds() + $$3.growthRounds();

         for (int $$7 = 0; $$7 < $$6; $$7++) {
            for (int $$8 = 0; $$8 < $$3.chargeCount(); $$8++) {
               $$5.addCursors($$2, $$3.amountPerCharge());
            }

            boolean $$9 = $$7 < $$3.spreadRounds();

            for (int $$10 = 0; $$10 < $$3.spreadAttempts(); $$10++) {
               $$5.updateCursors($$1, $$2, $$4, $$9);
            }

            $$5.clear();
         }

         BlockPos $$11 = $$2.below();
         if ($$4.nextFloat() <= $$3.catalystChance() && $$1.getBlockState($$11).isCollisionShapeFullBlock($$1, $$11)) {
            $$1.setBlock($$2, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
         }

         int $$12 = $$3.extraRareGrowths().sample($$4);

         for (int $$13 = 0; $$13 < $$12; $$13++) {
            BlockPos $$14 = $$2.offset($$4.nextInt(5) - 2, 0, $$4.nextInt(5) - 2);
            if ($$1.getBlockState($$14).isAir() && $$1.getBlockState($$14.below()).isFaceSturdy($$1, $$14.below(), Direction.UP)) {
               $$1.setBlock($$14, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, true), 3);
            }
         }

         return true;
      }
   }

   private boolean canSpreadFrom(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if ($$2.getBlock() instanceof SculkBehaviour) {
         return true;
      } else {
         return !$$2.isAir() && (!$$2.is(Blocks.WATER) || !$$2.getFluidState().isSource())
            ? false
            : Direction.stream().<BlockPos>map($$1::relative).anyMatch($$1x -> $$0.getBlockState($$1x).isCollisionShapeFullBlock($$0, $$1x));
      }
   }
}
