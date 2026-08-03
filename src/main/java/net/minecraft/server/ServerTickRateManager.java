package net.minecraft.server;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.TickRateManager;

public class ServerTickRateManager extends TickRateManager {
   private long remainingSprintTicks = 0L;
   private long sprintTickStartTime = 0L;
   private long sprintTimeSpend = 0L;
   private long scheduledCurrentSprintTicks = 0L;
   private boolean previousIsFrozen = false;
   private final net.minecraft.server.MinecraftServer server;

   public ServerTickRateManager(net.minecraft.server.MinecraftServer $$0) {
      this.server = $$0;
   }

   public boolean isSprinting() {
      return this.scheduledCurrentSprintTicks > 0L;
   }

   public void setFrozen(boolean $$0) {
      super.setFrozen($$0);
      this.updateStateToClients();
   }

   private void updateStateToClients() {
      this.server.getPlayerList().broadcastAll(ClientboundTickingStatePacket.from(this));
   }

   private void updateStepTicks() {
      this.server.getPlayerList().broadcastAll(ClientboundTickingStepPacket.from(this));
   }

   public boolean stepGameIfPaused(int $$0) {
      if (!this.isFrozen()) {
         return false;
      } else {
         this.frozenTicksToRun = $$0;
         this.updateStepTicks();
         return true;
      }
   }

   public boolean stopStepping() {
      if (this.frozenTicksToRun > 0) {
         this.frozenTicksToRun = 0;
         this.updateStepTicks();
         return true;
      } else {
         return false;
      }
   }

   public boolean stopSprinting() {
      if (this.remainingSprintTicks > 0L) {
         this.finishTickSprint();
         return true;
      } else {
         return false;
      }
   }

   public boolean requestGameToSprint(int $$0) {
      boolean $$1 = this.remainingSprintTicks > 0L;
      this.sprintTimeSpend = 0L;
      this.scheduledCurrentSprintTicks = $$0;
      this.remainingSprintTicks = $$0;
      this.previousIsFrozen = this.isFrozen();
      this.setFrozen(false);
      return $$1;
   }

   private void finishTickSprint() {
      long $$0 = this.scheduledCurrentSprintTicks - this.remainingSprintTicks;
      double $$1 = Math.max(1.0, (double)this.sprintTimeSpend) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
      int $$2 = (int)(TimeUtil.MILLISECONDS_PER_SECOND * $$0 / $$1);
      String $$3 = String.format(Locale.ROOT, "%.2f", $$0 == 0L ? this.millisecondsPerTick() : $$1 / $$0);
      this.scheduledCurrentSprintTicks = 0L;
      this.sprintTimeSpend = 0L;
      this.server.createCommandSourceStack().sendSuccess(() -> Component.translatable("commands.tick.sprint.report", new Object[]{$$2, $$3}), true);
      this.remainingSprintTicks = 0L;
      this.setFrozen(this.previousIsFrozen);
      this.server.onTickRateChanged();
   }

   public boolean checkShouldSprintThisTick() {
      if (!this.runGameElements) {
         return false;
      } else if (this.remainingSprintTicks > 0L) {
         this.sprintTickStartTime = System.nanoTime();
         this.remainingSprintTicks--;
         return true;
      } else {
         this.finishTickSprint();
         return false;
      }
   }

   public void endTickWork() {
      this.sprintTimeSpend = this.sprintTimeSpend + (System.nanoTime() - this.sprintTickStartTime);
   }

   public void setTickRate(float $$0) {
      super.setTickRate($$0);
      this.server.onTickRateChanged();
      this.updateStateToClients();
   }

   public void updateJoiningPlayer(ServerPlayer $$0) {
      $$0.connection.send(ClientboundTickingStatePacket.from(this));
      $$0.connection.send(ClientboundTickingStepPacket.from(this));
   }
}
