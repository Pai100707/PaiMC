package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class RunAroundLikeCrazyGoal extends Goal {
   private final AbstractHorse horse;
   private final double speedModifier;
   private double posX;
   private double posY;
   private double posZ;

   public RunAroundLikeCrazyGoal(AbstractHorse $$0, double $$1) {
      this.horse = $$0;
      this.speedModifier = $$1;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (!this.horse.isMobControlled() && !this.horse.isTamed() && this.horse.isVehicle()) {
         Vec3 $$0 = DefaultRandomPos.getPos(this.horse, 5, 4);
         if ($$0 == null) {
            return false;
         } else {
            this.posX = $$0.x;
            this.posY = $$0.y;
            this.posZ = $$0.z;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      this.horse.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
   }

   @Override
   public boolean canContinueToUse() {
      return !this.horse.isTamed() && !this.horse.getNavigation().isDone() && this.horse.isVehicle();
   }

   @Override
   public void tick() {
      if (!this.horse.isTamed() && this.horse.getRandom().nextInt(this.adjustedTickDelay(50)) == 0) {
         net.minecraft.world.entity.Entity $$0 = this.horse.getFirstPassenger();
         if ($$0 == null) {
            return;
         }

         if ($$0 instanceof Player $$1) {
            int $$2 = this.horse.getTemper();
            int $$3 = this.horse.getMaxTemper();
            if ($$3 > 0 && this.horse.getRandom().nextInt($$3) < $$2) {
               this.horse.tameWithName($$1);
               return;
            }

            this.horse.modifyTemper(5);
         }

         this.horse.ejectPassengers();
         this.horse.makeMad();
         this.horse.level().broadcastEntityEvent(this.horse, (byte)6);
      }
   }
}
