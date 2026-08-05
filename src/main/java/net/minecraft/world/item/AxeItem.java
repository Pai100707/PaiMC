package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class AxeItem extends net.minecraft.world.item.Item {
   protected static final Map<Block, Block> STRIPPABLES = new Builder()
      .put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD)
      .put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
      .put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD)
      .put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
      .put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD)
      .put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG)
      .put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD)
      .put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
      .put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
      .put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
      .put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD)
      .put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
      .put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD)
      .put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
      .put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD)
      .put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
      .put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM)
      .put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
      .put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM)
      .put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
      .put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD)
      .put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
      .put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK)
      .build();

   public AxeItem(net.minecraft.world.item.ToolMaterial $$0, float $$1, float $$2, net.minecraft.world.item.Item.Properties $$3) {
      super($$3.axe($$0, $$1, $$2));
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      Player $$3 = $$0.getPlayer();
      if (playerHasBlockingItemUseIntent($$0)) {
         return InteractionResult.PASS;
      } else {
         Optional<BlockState> $$4 = this.evaluateNewBlockState($$1, $$2, $$3, $$1.getBlockState($$2));
         if ($$4.isEmpty()) {
            return InteractionResult.PASS;
         } else {
            net.minecraft.world.item.ItemStack $$5 = $$0.getItemInHand();
            if ($$3 instanceof ServerPlayer) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)$$3, $$2, $$5);
            }

            $$1.setBlock($$2, $$4.get(), 11);
            $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, Context.of($$3, $$4.get()));
            if ($$3 != null) {
               $$5.hurtAndBreak(1, $$3, $$0.getHand().asEquipmentSlot());
            }

            return InteractionResult.SUCCESS;
         }
      }
   }

   private static boolean playerHasBlockingItemUseIntent(UseOnContext $$0) {
      Player $$1 = $$0.getPlayer();
      return $$0.getHand().equals(InteractionHand.MAIN_HAND) && $$1.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !$$1.isSecondaryUseActive();
   }

   private Optional<BlockState> evaluateNewBlockState(Level $$0, BlockPos $$1, Player $$2, BlockState $$3) {
      Optional<BlockState> $$4 = this.getStripped($$3);
      if ($$4.isPresent()) {
         $$0.playSound($$2, $$1, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
         return $$4;
      } else {
         Optional<BlockState> $$5 = WeatheringCopper.getPrevious($$3);
         if ($$5.isPresent()) {
            spawnSoundAndParticle($$0, $$1, $$2, $$3, SoundEvents.AXE_SCRAPE, 3005);
            return $$5;
         } else {
            Optional<BlockState> $$6 = Optional.ofNullable((Block)net.minecraft.world.item.HoneycombItem.WAX_OFF_BY_BLOCK.get().get($$3.getBlock()))
               .map($$1x -> $$1x.withPropertiesOf($$3));
            if ($$6.isPresent()) {
               spawnSoundAndParticle($$0, $$1, $$2, $$3, SoundEvents.AXE_WAX_OFF, 3004);
               return $$6;
            } else {
               return Optional.empty();
            }
         }
      }
   }

   private static void spawnSoundAndParticle(Level $$0, BlockPos $$1, Player $$2, BlockState $$3, SoundEvent $$4, int $$5) {
      $$0.playSound($$2, $$1, $$4, SoundSource.BLOCKS, 1.0F, 1.0F);
      $$0.levelEvent($$2, $$5, $$1, 0);
      if ($$3.getBlock() instanceof ChestBlock && $$3.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
         BlockPos $$6 = ChestBlock.getConnectedBlockPos($$1, $$3);
         $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$6, Context.of($$2, $$0.getBlockState($$6)));
         $$0.levelEvent($$2, $$5, $$6, 0);
      }
   }

   private Optional<BlockState> getStripped(BlockState $$0) {
      return Optional.ofNullable(STRIPPABLES.get($$0.getBlock()))
         .map($$1 -> (BlockState)$$1.defaultBlockState().setValue(RotatedPillarBlock.AXIS, (Axis)$$0.getValue(RotatedPillarBlock.AXIS)));
   }
}
