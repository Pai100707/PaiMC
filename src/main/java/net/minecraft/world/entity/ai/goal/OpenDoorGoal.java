package net.minecraft.world.entity.ai.goal;

public class OpenDoorGoal extends DoorInteractGoal {
   private final boolean closeDoor;
   private int forgetTime;

   public OpenDoorGoal(net.minecraft.world.entity.Mob $$0, boolean $$1) {
      super($$0);
      this.mob = $$0;
      this.closeDoor = $$1;
   }

   @Override
   public boolean canContinueToUse() {
      return this.closeDoor && this.forgetTime > 0 && super.canContinueToUse();
   }

   @Override
   public void start() {
      this.forgetTime = 20;
      this.setOpen(true);
   }

   @Override
   public void stop() {
      this.setOpen(false);
   }

   @Override
   public void tick() {
      this.forgetTime--;
      super.tick();
   }
}
