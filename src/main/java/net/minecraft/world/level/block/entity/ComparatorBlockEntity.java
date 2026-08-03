package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ComparatorBlockEntity extends BlockEntity {
   private static final int DEFAULT_OUTPUT = 0;
   private int output = 0;

   public ComparatorBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.COMPARATOR, $$0, $$1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.putInt("OutputSignal", this.output);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.output = $$0.getIntOr("OutputSignal", 0);
   }

   public int getOutputSignal() {
      return this.output;
   }

   public void setOutputSignal(int $$0) {
      this.output = $$0;
   }
}
