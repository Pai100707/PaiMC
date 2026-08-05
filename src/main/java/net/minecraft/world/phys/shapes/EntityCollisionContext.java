package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
   private final boolean descending;
   private final double entityBottom;
   private final boolean placement;
   private final ItemStack heldItem;
   private final boolean alwaysCollideWithFluid;
   
   private final Entity entity;

   protected EntityCollisionContext(boolean $$0, boolean $$1, double $$2, ItemStack $$3, boolean $$4, Entity $$5) {
      this.descending = $$0;
      this.placement = $$1;
      this.entityBottom = $$2;
      this.heldItem = $$3;
      this.alwaysCollideWithFluid = $$4;
      this.entity = $$5;
   }

   @Deprecated
   protected EntityCollisionContext(Entity $$0, boolean $$1, boolean $$2) {
      this($$0.isDescending(), $$2, $$0.getY(), $$0 instanceof LivingEntity $$3 ? $$3.getMainHandItem() : ItemStack.EMPTY, $$1, $$0);
   }

   @Override
   public boolean isHoldingItem(Item $$0) {
      return this.heldItem.is($$0);
   }

   @Override
   public boolean alwaysCollideWithFluid() {
      return this.alwaysCollideWithFluid;
   }

   @Override
   public boolean canStandOnFluid(FluidState $$0, FluidState $$1) {
      return !(this.entity instanceof LivingEntity $$2) ? false : $$2.canStandOnFluid($$1) && !$$0.getType().isSame($$1.getType());
   }

   @Override
   public VoxelShape getCollisionShape(BlockState $$0, CollisionGetter $$1, BlockPos $$2) {
      return $$0.getCollisionShape($$1, $$2, this);
   }

   @Override
   public boolean isDescending() {
      return this.descending;
   }

   @Override
   public boolean isAbove(VoxelShape $$0, BlockPos $$1, boolean $$2) {
      return this.entityBottom > $$1.getY() + $$0.max(Axis.Y) - 1.0E-5F;
   }

   
   public Entity getEntity() {
      return this.entity;
   }

   @Override
   public boolean isPlacement() {
      return this.placement;
   }

   protected static class Empty extends EntityCollisionContext {
      protected static final CollisionContext WITHOUT_FLUID_COLLISIONS = new EntityCollisionContext.Empty(false);
      protected static final CollisionContext WITH_FLUID_COLLISIONS = new EntityCollisionContext.Empty(true);

      public Empty(boolean $$0) {
         super(false, false, -Double.MAX_VALUE, ItemStack.EMPTY, $$0, null);
      }

      @Override
      public boolean isAbove(VoxelShape $$0, BlockPos $$1, boolean $$2) {
         return $$2;
      }
   }
}
