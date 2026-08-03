package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public interface SimpleWaterloggedBlock extends BucketPickup, LiquidBlockContainer {
   @Override
   default boolean canPlaceLiquid(@Nullable LivingEntity $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, Fluid $$4) {
      return $$4 == Fluids.WATER;
   }

   @Override
   default boolean placeLiquid(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      if (!$$2.getValue(BlockStateProperties.WATERLOGGED) && $$3.getType() == Fluids.WATER) {
         if (!$$0.isClientSide()) {
            $$0.setBlock($$1, $$2.setValue(BlockStateProperties.WATERLOGGED, true), 3);
            $$0.scheduleTick($$1, $$3.getType(), $$3.getType().getTickDelay($$0));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   default ItemStack pickupBlock(@Nullable LivingEntity $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      if ($$3.getValue(BlockStateProperties.WATERLOGGED)) {
         $$1.setBlock($$2, $$3.setValue(BlockStateProperties.WATERLOGGED, false), 3);
         if (!$$3.canSurvive($$1, $$2)) {
            $$1.destroyBlock($$2, true);
         }

         return new ItemStack(Items.WATER_BUCKET);
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Override
   default Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}
