package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CompassItem extends net.minecraft.world.item.Item {
   private static final Component LODESTONE_COMPASS_NAME = Component.translatable("item.minecraft.lodestone_compass");

   public CompassItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public boolean isFoil(net.minecraft.world.item.ItemStack $$0) {
      return $$0.has(DataComponents.LODESTONE_TRACKER) || super.isFoil($$0);
   }

   @Override
   public void inventoryTick(net.minecraft.world.item.ItemStack $$0, ServerLevel $$1, Entity $$2, EquipmentSlot $$3) {
      LodestoneTracker $$4 = (LodestoneTracker)$$0.get(DataComponents.LODESTONE_TRACKER);
      if ($$4 != null) {
         LodestoneTracker $$5 = $$4.tick($$1);
         if ($$5 != $$4) {
            $$0.set(DataComponents.LODESTONE_TRACKER, $$5);
         }
      }
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      BlockPos $$1 = $$0.getClickedPos();
      Level $$2 = $$0.getLevel();
      if (!$$2.getBlockState($$1).is(Blocks.LODESTONE)) {
         return super.useOn($$0);
      } else {
         $$2.playSound(null, $$1, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
         Player $$3 = $$0.getPlayer();
         net.minecraft.world.item.ItemStack $$4 = $$0.getItemInHand();
         boolean $$5 = !$$3.hasInfiniteMaterials() && $$4.getCount() == 1;
         LodestoneTracker $$6 = new LodestoneTracker(Optional.of(GlobalPos.of($$2.dimension(), $$1)), true);
         if ($$5) {
            $$4.set(DataComponents.LODESTONE_TRACKER, $$6);
         } else {
            net.minecraft.world.item.ItemStack $$7 = $$4.transmuteCopy(net.minecraft.world.item.Items.COMPASS, 1);
            $$4.consume(1, $$3);
            $$7.set(DataComponents.LODESTONE_TRACKER, $$6);
            if (!$$3.getInventory().add($$7)) {
               $$3.drop($$7, false);
            }
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public Component getName(net.minecraft.world.item.ItemStack $$0) {
      return $$0.has(DataComponents.LODESTONE_TRACKER) ? LODESTONE_COMPASS_NAME : super.getName($$0);
   }
}
