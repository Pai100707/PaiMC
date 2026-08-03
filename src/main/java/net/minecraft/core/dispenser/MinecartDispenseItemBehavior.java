package net.minecraft.core.dispenser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class MinecartDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
   private final EntityType<? extends AbstractMinecart> entityType;

   public MinecartDispenseItemBehavior(EntityType<? extends AbstractMinecart> $$0) {
      this.entityType = $$0;
   }

   @Override
   public ItemStack execute(BlockSource $$0, ItemStack $$1) {
      net.minecraft.core.Direction $$2 = (net.minecraft.core.Direction)$$0.state().getValue(DispenserBlock.FACING);
      ServerLevel $$3 = $$0.level();
      Vec3 $$4 = $$0.center();
      double $$5 = $$4.x() + $$2.getStepX() * 1.125;
      double $$6 = Math.floor($$4.y()) + $$2.getStepY();
      double $$7 = $$4.z() + $$2.getStepZ() * 1.125;
      net.minecraft.core.BlockPos $$8 = $$0.pos().relative($$2);
      BlockState $$9 = $$3.getBlockState($$8);
      double $$10;
      if ($$9.is(BlockTags.RAILS)) {
         if (getRailShape($$9).isSlope()) {
            $$10 = 0.6;
         } else {
            $$10 = 0.1;
         }
      } else {
         if (!$$9.isAir()) {
            return this.defaultDispenseItemBehavior.dispense($$0, $$1);
         }

         BlockState $$12 = $$3.getBlockState($$8.below());
         if (!$$12.is(BlockTags.RAILS)) {
            return this.defaultDispenseItemBehavior.dispense($$0, $$1);
         }

         if ($$2 != net.minecraft.core.Direction.DOWN && getRailShape($$12).isSlope()) {
            $$10 = -0.4;
         } else {
            $$10 = -0.9;
         }
      }

      Vec3 $$17 = new Vec3($$5, $$6 + $$10, $$7);
      AbstractMinecart $$18 = AbstractMinecart.createMinecart($$3, $$17.x, $$17.y, $$17.z, this.entityType, EntitySpawnReason.DISPENSER, $$1, null);
      if ($$18 != null) {
         $$3.addFreshEntity($$18);
         $$1.shrink(1);
      }

      return $$1;
   }

   private static RailShape getRailShape(BlockState $$0) {
      return $$0.getBlock() instanceof BaseRailBlock $$1 ? (RailShape)$$0.getValue($$1.getShapeProperty()) : RailShape.NORTH_SOUTH;
   }

   @Override
   protected void playSound(BlockSource $$0) {
      $$0.level().levelEvent(1000, $$0.pos(), 0);
   }
}
