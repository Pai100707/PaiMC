package net.minecraft.world.entity.ai.goal;

import java.util.function.Predicate;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;

public class BreakDoorGoal extends DoorInteractGoal {
   private static final int DEFAULT_DOOR_BREAK_TIME = 240;
   private final Predicate<Difficulty> validDifficulties;
   protected int breakTime;
   protected int lastBreakProgress = -1;
   protected int doorBreakTime = -1;

   public BreakDoorGoal(net.minecraft.world.entity.Mob $$0, Predicate<Difficulty> $$1) {
      super($$0);
      this.validDifficulties = $$1;
   }

   public BreakDoorGoal(net.minecraft.world.entity.Mob $$0, int $$1, Predicate<Difficulty> $$2) {
      this($$0, $$2);
      this.doorBreakTime = $$1;
   }

   protected int getDoorBreakTime() {
      return Math.max(240, this.doorBreakTime);
   }

   @Override
   public boolean canUse() {
      if (!super.canUse()) {
         return false;
      } else {
         return !getServerLevel(this.mob).getGameRules().get(GameRules.MOB_GRIEFING)
            ? false
            : this.isValidDifficulty(this.mob.level().getDifficulty()) && !this.isOpen();
      }
   }

   @Override
   public void start() {
      super.start();
      this.breakTime = 0;
   }

   @Override
   public boolean canContinueToUse() {
      return this.breakTime <= this.getDoorBreakTime()
         && !this.isOpen()
         && this.doorPos.closerToCenterThan(this.mob.position(), 2.0)
         && this.isValidDifficulty(this.mob.level().getDifficulty());
   }

   @Override
   public void stop() {
      super.stop();
      this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, -1);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.mob.getRandom().nextInt(20) == 0) {
         this.mob.level().levelEvent(1019, this.doorPos, 0);
         if (!this.mob.swinging) {
            this.mob.swing(this.mob.getUsedItemHand());
         }
      }

      this.breakTime++;
      int $$0 = (int)((float)this.breakTime / this.getDoorBreakTime() * 10.0F);
      if ($$0 != this.lastBreakProgress) {
         this.mob.level().destroyBlockProgress(this.mob.getId(), this.doorPos, $$0);
         this.lastBreakProgress = $$0;
      }

      if (this.breakTime == this.getDoorBreakTime() && this.isValidDifficulty(this.mob.level().getDifficulty())) {
         this.mob.level().removeBlock(this.doorPos, false);
         this.mob.level().levelEvent(1021, this.doorPos, 0);
         this.mob.level().levelEvent(2001, this.doorPos, Block.getId(this.mob.level().getBlockState(this.doorPos)));
      }
   }

   private boolean isValidDifficulty(Difficulty $$0) {
      return this.validDifficulties.test($$0);
   }
}
