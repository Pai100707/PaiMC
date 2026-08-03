package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Portal.Transition;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;

public class PortalProcessor {
   private final Portal portal;
   private BlockPos entryPosition;
   private int portalTime;
   private boolean insidePortalThisTick;

   public PortalProcessor(Portal $$0, BlockPos $$1) {
      this.portal = $$0;
      this.entryPosition = $$1;
      this.insidePortalThisTick = true;
   }

   public boolean processPortalTeleportation(ServerLevel $$0, net.minecraft.world.entity.Entity $$1, boolean $$2) {
      if (!this.insidePortalThisTick) {
         this.decayTick();
         return false;
      } else {
         this.insidePortalThisTick = false;
         return $$2 && this.portalTime++ >= this.portal.getPortalTransitionTime($$0, $$1);
      }
   }

   @Nullable
   public TeleportTransition getPortalDestination(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      return this.portal.getPortalDestination($$0, $$1, this.entryPosition);
   }

   public Transition getPortalLocalTransition() {
      return this.portal.getLocalTransition();
   }

   private void decayTick() {
      this.portalTime = Math.max(this.portalTime - 4, 0);
   }

   public boolean hasExpired() {
      return this.portalTime <= 0;
   }

   public BlockPos getEntryPosition() {
      return this.entryPosition;
   }

   public void updateEntryPosition(BlockPos $$0) {
      this.entryPosition = $$0;
   }

   public int getPortalTime() {
      return this.portalTime;
   }

   public boolean isInsidePortalThisTick() {
      return this.insidePortalThisTick;
   }

   public void setAsInsidePortalThisTick(boolean $$0) {
      this.insidePortalThisTick = $$0;
   }

   public boolean isSamePortal(Portal $$0) {
      return this.portal == $$0;
   }
}
