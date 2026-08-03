package net.minecraft.world.entity;

public interface PlayerRideableJumping extends net.minecraft.world.entity.PlayerRideable {
   void onPlayerJump(int var1);

   boolean canJump();

   void handleStartJump(int var1);

   void handleStopJump();

   default int getJumpCooldown() {
      return 0;
   }

   default float getPlayerJumpPendingScale(int $$0) {
      return $$0 >= 90 ? 1.0F : 0.4F + 0.4F * $$0 / 90.0F;
   }
}
