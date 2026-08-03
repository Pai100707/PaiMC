package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;

public class PumpkinBlock extends Block {
   public static final MapCodec<PumpkinBlock> CODEC = simpleCodec(PumpkinBlock::new);

   @Override
   public MapCodec<PumpkinBlock> codec() {
      return CODEC;
   }

   protected PumpkinBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if (!$$0.is(Items.SHEARS)) {
         return super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6);
      } else if ($$2 instanceof ServerLevel $$7) {
         Direction $$9 = $$6.getDirection();
         Direction $$10 = $$9.getAxis() == Axis.Y ? $$4.getDirection().getOpposite() : $$9;
         dropFromBlockInteractLootTable($$7, BuiltInLootTables.CARVE_PUMPKIN, $$1, $$2.getBlockEntity($$3), $$0, $$4, ($$3x, $$4x) -> {
            ItemEntity $$5x = new ItemEntity($$2, $$3.getX() + 0.5 + $$10.getStepX() * 0.65, $$3.getY() + 0.1, $$3.getZ() + 0.5 + $$10.getStepZ() * 0.65, $$4x);
            $$5x.setDeltaMovement(0.05 * $$10.getStepX() + $$2.random.nextDouble() * 0.02, 0.05, 0.05 * $$10.getStepZ() + $$2.random.nextDouble() * 0.02);
            $$2.addFreshEntity($$5x);
         });
         $$2.playSound(null, $$3, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$2.setBlock($$3, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, $$10), 11);
         $$0.hurtAndBreak(1, $$4, $$5.asEquipmentSlot());
         $$2.gameEvent($$4, GameEvent.SHEAR, $$3);
         $$4.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.SUCCESS;
      }
   }
}
