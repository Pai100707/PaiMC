package net.minecraft.world.item;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class DebugStickItem extends net.minecraft.world.item.Item {
   public DebugStickItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public boolean canDestroyBlock(net.minecraft.world.item.ItemStack $$0, BlockState $$1, Level $$2, BlockPos $$3, LivingEntity $$4) {
      if (!$$2.isClientSide() && $$4 instanceof Player $$5) {
         this.handleInteraction($$5, $$1, $$2, $$3, false, $$0);
      }

      return false;
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Player $$1 = $$0.getPlayer();
      Level $$2 = $$0.getLevel();
      if (!$$2.isClientSide() && $$1 != null) {
         BlockPos $$3 = $$0.getClickedPos();
         if (!this.handleInteraction($$1, $$2.getBlockState($$3), $$2, $$3, true, $$0.getItemInHand())) {
            return InteractionResult.FAIL;
         }
      }

      return InteractionResult.SUCCESS;
   }

   private boolean handleInteraction(Player $$0, BlockState $$1, LevelAccessor $$2, BlockPos $$3, boolean $$4, net.minecraft.world.item.ItemStack $$5) {
      if (!$$0.canUseGameMasterBlocks()) {
         return false;
      } else {
         Holder<Block> $$6 = $$1.getBlockHolder();
         StateDefinition<Block, BlockState> $$7 = ((Block)$$6.value()).getStateDefinition();
         Collection<Property<?>> $$8 = $$7.getProperties();
         if ($$8.isEmpty()) {
            message($$0, Component.translatable(this.descriptionId + ".empty", new Object[]{$$6.getRegisteredName()}));
            return false;
         } else {
            DebugStickState $$9 = (DebugStickState)$$5.get(DataComponents.DEBUG_STICK_STATE);
            if ($$9 == null) {
               return false;
            } else {
               Property<?> $$10 = $$9.properties().get($$6);
               if ($$4) {
                  if ($$10 == null) {
                     $$10 = $$8.iterator().next();
                  }

                  BlockState $$11 = cycleState($$1, $$10, $$0.isSecondaryUseActive());
                  $$2.setBlock($$3, $$11, 18);
                  message($$0, Component.translatable(this.descriptionId + ".update", new Object[]{$$10.getName(), getNameHelper($$11, $$10)}));
               } else {
                  $$10 = getRelative($$8, $$10, $$0.isSecondaryUseActive());
                  $$5.set(DataComponents.DEBUG_STICK_STATE, $$9.withProperty($$6, $$10));
                  message($$0, Component.translatable(this.descriptionId + ".select", new Object[]{$$10.getName(), getNameHelper($$1, $$10)}));
               }

               return true;
            }
         }
      }
   }

   private static <T extends Comparable<T>> BlockState cycleState(BlockState $$0, Property<T> $$1, boolean $$2) {
      return (BlockState)$$0.setValue($$1, getRelative($$1.getPossibleValues(), $$0.getValue($$1), $$2));
   }

   private static <T> T getRelative(Iterable<T> $$0, T $$1, boolean $$2) {
      return (T)($$2 ? Util.findPreviousInIterable($$0, $$1) : Util.findNextInIterable($$0, $$1));
   }

   private static void message(Player $$0, Component $$1) {
      ((ServerPlayer)$$0).sendSystemMessage($$1, true);
   }

   private static <T extends Comparable<T>> String getNameHelper(BlockState $$0, Property<T> $$1) {
      return $$1.getName($$0.getValue($$1));
   }
}
