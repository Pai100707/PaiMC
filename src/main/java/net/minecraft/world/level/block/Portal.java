package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;

public interface Portal {
   default int getPortalTransitionTime(ServerLevel $$0, Entity $$1) {
      return 0;
   }

   
   TeleportTransition getPortalDestination(ServerLevel var1, Entity var2, BlockPos var3);

   default Portal.Transition getLocalTransition() {
      return Portal.Transition.NONE;
   }

   public static enum Transition {
      CONFUSION,
      NONE;
   }
}
