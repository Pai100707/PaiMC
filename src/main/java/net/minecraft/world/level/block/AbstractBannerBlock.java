package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.color = $$0;
   }

   @Override
   protected abstract MapCodec<? extends AbstractBannerBlock> codec();

   @Override
   public boolean isPossibleToRespawnInThis(BlockState $$0) {
      return true;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new BannerBlockEntity($$0, $$1, this.color);
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return $$0.getBlockEntity($$1) instanceof BannerBlockEntity $$4 ? $$4.getItem() : super.getCloneItemStack($$0, $$1, $$2, $$3);
   }

   public DyeColor getColor() {
      return this.color;
   }
}
