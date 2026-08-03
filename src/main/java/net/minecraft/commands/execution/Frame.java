package net.minecraft.commands.execution;

public record Frame(int depth, net.minecraft.commands.CommandResultCallback returnValueConsumer, Frame.FrameControl frameControl) {
   public void returnSuccess(int $$0) {
      this.returnValueConsumer.onSuccess($$0);
   }

   public void returnFailure() {
      this.returnValueConsumer.onFailure();
   }

   public void discard() {
      this.frameControl.discard();
   }

   @FunctionalInterface
   public interface FrameControl {
      void discard();
   }
}
