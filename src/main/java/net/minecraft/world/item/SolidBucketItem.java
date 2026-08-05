package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class SolidBucketItem extends net.minecraft.world.item.BlockItem implements net.minecraft.world.item.DispensibleContainerItem {
   private final SoundEvent placeSound;

   public SolidBucketItem(Block $$0, SoundEvent $$1, net.minecraft.world.item.Item.Properties $$2) {
      super($$0, $$2);
      this.placeSound = $$1;
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      InteractionResult $$1 = super.useOn($$0);
      Player $$2 = $$0.getPlayer();
      if ($$1.consumesAction() && $$2 != null) {
         $$2.setItemInHand($$0.getHand(), net.minecraft.world.item.BucketItem.getEmptySuccessItem($$0.getItemInHand(), $$2));
      }

      return $$1;
   }

   @Override
   protected SoundEvent getPlaceSound(BlockState $$0) {
      return this.placeSound;
   }

   @Override
   public boolean emptyContents(LivingEntity $$0, Level $$1, BlockPos $$2, BlockHitResult $$3) {
      if ($$1.isInWorldBounds($$2) && $$1.isEmptyBlock($$2)) {
         if (!$$1.isClientSide()) {
            $$1.setBlock($$2, this.getBlock().defaultBlockState(), 3);
         }

         $$1.gameEvent($$0, GameEvent.FLUID_PLACE, $$2);
         $$1.playSound($$0, $$2, this.placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
