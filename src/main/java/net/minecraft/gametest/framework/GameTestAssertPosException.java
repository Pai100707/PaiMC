package net.minecraft.gametest.framework;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class GameTestAssertPosException extends GameTestAssertException {
   private final BlockPos absolutePos;
   private final BlockPos relativePos;

   public GameTestAssertPosException(Component $$0, BlockPos $$1, BlockPos $$2, int $$3) {
      super($$0, $$3);
      this.absolutePos = $$1;
      this.relativePos = $$2;
   }

   @Override
   public Component getDescription() {
      return Component.translatable(
         "test.error.position",
         new Object[]{
            this.message,
            this.absolutePos.getX(),
            this.absolutePos.getY(),
            this.absolutePos.getZ(),
            this.relativePos.getX(),
            this.relativePos.getY(),
            this.relativePos.getZ(),
            this.tick
         }
      );
   }

   public Component getMessageToShowAtBlock() {
      return this.message;
   }

   
   public BlockPos getRelativePos() {
      return this.relativePos;
   }

   
   public BlockPos getAbsolutePos() {
      return this.absolutePos;
   }
}
