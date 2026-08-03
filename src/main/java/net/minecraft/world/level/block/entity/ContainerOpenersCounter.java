package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
   private static final int CHECK_TICK_DELAY = 5;
   private int openCount;
   private double maxInteractionRange;

   protected abstract void onOpen(net.minecraft.world.level.Level var1, BlockPos var2, BlockState var3);

   protected abstract void onClose(net.minecraft.world.level.Level var1, BlockPos var2, BlockState var3);

   protected abstract void openerCountChanged(net.minecraft.world.level.Level var1, BlockPos var2, BlockState var3, int var4, int var5);

   public abstract boolean isOwnContainer(Player var1);

   public void incrementOpeners(LivingEntity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, double $$4) {
      int $$5 = this.openCount++;
      if ($$5 == 0) {
         this.onOpen($$1, $$2, $$3);
         $$1.gameEvent($$0, GameEvent.CONTAINER_OPEN, $$2);
         scheduleRecheck($$1, $$2, $$3);
      }

      this.openerCountChanged($$1, $$2, $$3, $$5, this.openCount);
      this.maxInteractionRange = Math.max($$4, this.maxInteractionRange);
   }

   public void decrementOpeners(LivingEntity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3) {
      int $$4 = this.openCount--;
      if (this.openCount == 0) {
         this.onClose($$1, $$2, $$3);
         $$1.gameEvent($$0, GameEvent.CONTAINER_CLOSE, $$2);
         this.maxInteractionRange = 0.0;
      }

      this.openerCountChanged($$1, $$2, $$3, $$4, this.openCount);
   }

   public List<ContainerUser> getEntitiesWithContainerOpen(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      double $$2 = this.maxInteractionRange + 4.0;
      AABB $$3 = new AABB($$1).inflate($$2);
      return $$0.getEntities((Entity)null, $$3, $$1x -> this.hasContainerOpen($$1x, $$1))
         .stream()
         .map($$0x -> (ContainerUser)$$0x)
         .collect(Collectors.toList());
   }

   private boolean hasContainerOpen(Entity $$0, BlockPos $$1) {
      return $$0 instanceof ContainerUser $$2 && !$$2.getLivingEntity().isSpectator() ? $$2.hasContainerOpen(this, $$1) : false;
   }

   public void recheckOpeners(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      List<ContainerUser> $$3 = this.getEntitiesWithContainerOpen($$0, $$1);
      this.maxInteractionRange = 0.0;

      for (ContainerUser $$4 : $$3) {
         this.maxInteractionRange = Math.max($$4.getContainerInteractionRange(), this.maxInteractionRange);
      }

      int $$5 = $$3.size();
      int $$6 = this.openCount;
      if ($$6 != $$5) {
         boolean $$7 = $$5 != 0;
         boolean $$8 = $$6 != 0;
         if ($$7 && !$$8) {
            this.onOpen($$0, $$1, $$2);
            $$0.gameEvent(null, GameEvent.CONTAINER_OPEN, $$1);
         } else if (!$$7) {
            this.onClose($$0, $$1, $$2);
            $$0.gameEvent(null, GameEvent.CONTAINER_CLOSE, $$1);
         }

         this.openCount = $$5;
      }

      this.openerCountChanged($$0, $$1, $$2, $$6, $$5);
      if ($$5 > 0) {
         scheduleRecheck($$0, $$1, $$2);
      }
   }

   public int getOpenerCount() {
      return this.openCount;
   }

   private static void scheduleRecheck(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      $$0.scheduleTick($$1, $$2.getBlock(), 5);
   }
}
