package net.minecraft.world.item;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class ShovelItem extends net.minecraft.world.item.Item {
   protected static final Map<Block, BlockState> FLATTENABLES = Maps.newHashMap(
      new Builder()
         .put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.defaultBlockState())
         .put(Blocks.DIRT, Blocks.DIRT_PATH.defaultBlockState())
         .put(Blocks.PODZOL, Blocks.DIRT_PATH.defaultBlockState())
         .put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.defaultBlockState())
         .put(Blocks.MYCELIUM, Blocks.DIRT_PATH.defaultBlockState())
         .put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.defaultBlockState())
         .build()
   );

   public ShovelItem(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2, net.minecraft.world.item.Item.Properties $$3) {
      super($$3.shovel($$0, $$1, $$2));
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      if ($$0.getClickedFace() == Direction.DOWN) {
         return InteractionResult.PASS;
      } else {
         Player $$4 = $$0.getPlayer();
         BlockState $$5 = FLATTENABLES.get($$3.getBlock());
         BlockState $$6 = null;
         if ($$5 != null && $$1.getBlockState($$2.above()).isAir()) {
            $$1.playSound($$4, $$2, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            $$6 = $$5;
         } else if ($$3.getBlock() instanceof CampfireBlock && (Boolean)$$3.getValue(CampfireBlock.LIT)) {
            if (!$$1.isClientSide()) {
               $$1.levelEvent(null, 1009, $$2, 0);
            }

            CampfireBlock.dowse($$0.getPlayer(), $$1, $$2, $$3);
            $$6 = (BlockState)$$3.setValue(CampfireBlock.LIT, false);
         }

         if ($$6 != null) {
            if (!$$1.isClientSide()) {
               $$1.setBlock($$2, $$6, 11);
               $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, Context.of($$4, $$6));
               if ($$4 != null) {
                  $$0.getItemInHand().hurtAndBreak(1, $$4, $$0.getHand().asEquipmentSlot());
               }
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }
}
