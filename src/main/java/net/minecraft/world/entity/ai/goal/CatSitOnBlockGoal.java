package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
   private final Cat cat;

   public CatSitOnBlockGoal(Cat $$0, double $$1) {
      super($$0, $$1, 8);
      this.cat = $$0;
   }

   @Override
   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && super.canUse();
   }

   @Override
   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   @Override
   public void stop() {
      super.stop();
      this.cat.setInSittingPose(false);
   }

   @Override
   public void tick() {
      super.tick();
      this.cat.setInSittingPose(this.isReachedTarget());
   }

   @Override
   protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
      if (!$$0.isEmptyBlock($$1.above())) {
         return false;
      } else {
         BlockState $$2 = $$0.getBlockState($$1);
         if ($$2.is(Blocks.CHEST)) {
            return ChestBlockEntity.getOpenCount($$0, $$1) < 1;
         } else {
            return $$2.is(Blocks.FURNACE) && $$2.getValue(FurnaceBlock.LIT)
               ? true
               : $$2.is(BlockTags.BEDS, $$0x -> $$0x.getOptionalValue(BedBlock.PART).map($$0xx -> $$0xx != BedPart.HEAD).orElse(true));
         }
      }
   }
}
