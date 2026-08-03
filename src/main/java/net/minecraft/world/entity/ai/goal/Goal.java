package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public abstract class Goal {
   private final EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

   public abstract boolean canUse();

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return true;
   }

   public void start() {
   }

   public void stop() {
   }

   public boolean requiresUpdateEveryTick() {
      return false;
   }

   public void tick() {
   }

   public void setFlags(EnumSet<Goal.Flag> $$0) {
      this.flags.clear();
      this.flags.addAll($$0);
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName();
   }

   public EnumSet<Goal.Flag> getFlags() {
      return this.flags;
   }

   protected int adjustedTickDelay(int $$0) {
      return this.requiresUpdateEveryTick() ? $$0 : reducedTickDelay($$0);
   }

   protected static int reducedTickDelay(int $$0) {
      return Mth.positiveCeilDiv($$0, 2);
   }

   protected static ServerLevel getServerLevel(net.minecraft.world.entity.Entity $$0) {
      return (ServerLevel)$$0.level();
   }

   protected static ServerLevel getServerLevel(Level $$0) {
      return (ServerLevel)$$0;
   }

   public static enum Flag {
      MOVE,
      LOOK,
      JUMP,
      TARGET;
   }
}
