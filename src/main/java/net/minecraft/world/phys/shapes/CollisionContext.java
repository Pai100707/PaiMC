package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public interface CollisionContext {
   static CollisionContext empty() {
      return EntityCollisionContext.Empty.WITHOUT_FLUID_COLLISIONS;
   }

   static CollisionContext emptyWithFluidCollisions() {
      return EntityCollisionContext.Empty.WITH_FLUID_COLLISIONS;
   }

   static CollisionContext of(Entity $$0) {
      return (CollisionContext)(switch ($$0) {
         case AbstractMinecart $$1 -> AbstractMinecart.useExperimentalMovement($$1.level())
            ? new MinecartCollisionContext($$1, false)
            : new EntityCollisionContext($$0, false, false);
         default -> new EntityCollisionContext($$0, false, false);
      });
   }

   static CollisionContext of(Entity $$0, boolean $$1) {
      return new EntityCollisionContext($$0, $$1, false);
   }

   static CollisionContext placementContext(@Nullable Player $$0) {
      return new EntityCollisionContext(
         $$0 != null ? $$0.isDescending() : false,
         true,
         $$0 != null ? $$0.getY() : -Double.MAX_VALUE,
         $$0 instanceof LivingEntity ? $$0.getMainHandItem() : ItemStack.EMPTY,
         false,
         $$0
      );
   }

   static CollisionContext withPosition(@Nullable Entity $$0, double $$1) {
      return new EntityCollisionContext(
         $$0 != null ? $$0.isDescending() : false,
         true,
         $$0 != null ? $$1 : -Double.MAX_VALUE,
         $$0 instanceof LivingEntity $$2 ? $$2.getMainHandItem() : ItemStack.EMPTY,
         false,
         $$0
      );
   }

   boolean isDescending();

   boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

   boolean isHoldingItem(Item var1);

   boolean alwaysCollideWithFluid();

   boolean canStandOnFluid(FluidState var1, FluidState var2);

   VoxelShape getCollisionShape(BlockState var1, CollisionGetter var2, BlockPos var3);

   default boolean isPlacement() {
      return false;
   }
}
