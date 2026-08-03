package net.minecraft.world.entity.ai.control;

public class JumpControl implements Control {
   private final net.minecraft.world.entity.Mob mob;
   protected boolean jump;

   public JumpControl(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
   }

   public void jump() {
      this.jump = true;
   }

   public void tick() {
      this.mob.setJumping(this.jump);
      this.jump = false;
   }
}
