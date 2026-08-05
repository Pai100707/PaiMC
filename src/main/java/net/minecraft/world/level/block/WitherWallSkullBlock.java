package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WitherWallSkullBlock extends WallSkullBlock {
   public static final MapCodec<WitherWallSkullBlock> CODEC = simpleCodec(WitherWallSkullBlock::new);

   @Override
   public MapCodec<WitherWallSkullBlock> codec() {
      return CODEC;
   }

   protected WitherWallSkullBlock(BlockBehaviour.Properties $$0) {
      super(SkullBlock.Types.WITHER_SKELETON, $$0);
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      WitherSkullBlock.checkSpawn($$0, $$1);
   }
}
