package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
   VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);
   BooleanProperty BERRIES = BlockStateProperties.BERRIES;

   static InteractionResult use(Entity $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3) {
      if ($$1.getValue(BERRIES)) {
         if ($$2 instanceof ServerLevel $$4) {
            Block.dropFromBlockInteractLootTable(
               $$4, BuiltInLootTables.HARVEST_CAVE_VINE, $$1, $$2.getBlockEntity($$3), null, $$0, ($$1x, $$2x) -> Block.popResource($$1x, $$3, $$2x)
            );
            float $$5 = Mth.randomBetween($$4.random, 0.8F, 1.2F);
            $$4.playSound(null, $$3, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, $$5);
            BlockState $$6 = $$1.setValue(BERRIES, false);
            $$4.setBlock($$3, $$6, 2);
            $$4.gameEvent(GameEvent.BLOCK_CHANGE, $$3, GameEvent.Context.of($$0, $$6));
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   static boolean hasGlowBerries(BlockState $$0) {
      return $$0.hasProperty(BERRIES) && $$0.getValue(BERRIES);
   }

   static ToIntFunction<BlockState> emission(int $$0) {
      return $$1 -> $$1.getValue(BlockStateProperties.BERRIES) ? $$0 : 0;
   }
}
