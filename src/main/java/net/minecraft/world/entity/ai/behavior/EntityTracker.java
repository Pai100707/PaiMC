package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;

public class EntityTracker implements PositionTracker {
   private final net.minecraft.world.entity.Entity entity;
   private final boolean trackEyeHeight;
   private final boolean targetEyeHeight;

   public EntityTracker(net.minecraft.world.entity.Entity $$0, boolean $$1) {
      this($$0, $$1, false);
   }

   public EntityTracker(net.minecraft.world.entity.Entity $$0, boolean $$1, boolean $$2) {
      this.entity = $$0;
      this.trackEyeHeight = $$1;
      this.targetEyeHeight = $$2;
   }

   @Override
   public Vec3 currentPosition() {
      return this.trackEyeHeight ? this.entity.position().add(0.0, this.entity.getEyeHeight(), 0.0) : this.entity.position();
   }

   @Override
   public BlockPos currentBlockPosition() {
      return this.targetEyeHeight ? BlockPos.containing(this.entity.getEyePosition()) : this.entity.blockPosition();
   }

   @Override
   public boolean isVisibleBy(net.minecraft.world.entity.LivingEntity $$0) {
      if (this.entity instanceof net.minecraft.world.entity.LivingEntity $$1) {
         if (!$$1.isAlive()) {
            return false;
         } else {
            Optional<NearestVisibleLivingEntities> $$3 = $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
            return $$3.isPresent() && $$3.get().contains($$1);
         }
      } else {
         return true;
      }
   }

   public net.minecraft.world.entity.Entity getEntity() {
      return this.entity;
   }

   @Override
   public String toString() {
      return "EntityTracker for " + this.entity;
   }
}
