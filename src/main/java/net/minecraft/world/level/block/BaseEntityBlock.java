package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseEntityBlock extends Block implements EntityBlock {
   protected BaseEntityBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected abstract MapCodec<? extends BaseEntityBlock> codec();

   @Override
   protected boolean triggerEvent(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, int $$3, int $$4) {
      super.triggerEvent($$0, $$1, $$2, $$3, $$4);
      BlockEntity $$5 = $$1.getBlockEntity($$2);
      return $$5 == null ? false : $$5.triggerEvent($$3, $$4);
   }

   
   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      BlockEntity $$3 = $$1.getBlockEntity($$2);
      return $$3 instanceof MenuProvider ? (MenuProvider)$$3 : null;
   }

   
   protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
      BlockEntityType<A> $$0, BlockEntityType<E> $$1, BlockEntityTicker<? super E> $$2
   ) {
      return $$1 == $$0 ? $$2 : null;
   }
}
