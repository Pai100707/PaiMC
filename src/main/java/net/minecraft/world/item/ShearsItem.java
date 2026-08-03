package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class ShearsItem extends net.minecraft.world.item.Item {
   public ShearsItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   public static Tool createToolProperties() {
      HolderGetter<Block> $$0 = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
      return new Tool(
         List.of(
            Tool.Rule.minesAndDrops(HolderSet.direct(new Holder[]{Blocks.COBWEB.builtInRegistryHolder()}), 15.0F),
            Tool.Rule.overrideSpeed($$0.getOrThrow(BlockTags.LEAVES), 15.0F),
            Tool.Rule.overrideSpeed($$0.getOrThrow(BlockTags.WOOL), 5.0F),
            Tool.Rule.overrideSpeed(HolderSet.direct(new Holder[]{Blocks.VINE.builtInRegistryHolder(), Blocks.GLOW_LICHEN.builtInRegistryHolder()}), 2.0F)
         ),
         1.0F,
         1,
         true
      );
   }

   @Override
   public boolean mineBlock(net.minecraft.world.item.ItemStack $$0, Level $$1, BlockState $$2, BlockPos $$3, LivingEntity $$4) {
      Tool $$5 = (Tool)$$0.get(DataComponents.TOOL);
      if ($$5 == null) {
         return false;
      } else {
         if (!$$1.isClientSide() && !$$2.is(BlockTags.FIRE) && $$5.damagePerBlock() > 0) {
            $$0.hurtAndBreak($$5.damagePerBlock(), $$4, EquipmentSlot.MAINHAND);
         }

         return true;
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      if ($$3.getBlock() instanceof GrowingPlantHeadBlock $$5 && !$$5.isMaxAge($$3)) {
         Player $$6 = $$0.getPlayer();
         net.minecraft.world.item.ItemStack $$7 = $$0.getItemInHand();
         if ($$6 instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)$$6, $$2, $$7);
         }

         $$1.playSound($$6, $$2, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
         BlockState $$8 = $$5.getMaxAgeState($$3);
         $$1.setBlockAndUpdate($$2, $$8);
         $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, Context.of($$0.getPlayer(), $$8));
         if ($$6 != null) {
            $$7.hurtAndBreak(1, $$6, $$0.getHand().asEquipmentSlot());
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.useOn($$0);
      }
   }
}
