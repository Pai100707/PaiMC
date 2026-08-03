package net.minecraft.core.dispenser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.BeeReleaseStatus;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class ShearsDispenseItemBehavior extends OptionalDispenseItemBehavior {
   @Override
   protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
      ServerLevel $$2 = $$0.level();
      if (!$$2.isClientSide()) {
         net.minecraft.core.BlockPos $$3 = $$0.pos().relative((net.minecraft.core.Direction)$$0.state().getValue(DispenserBlock.FACING));
         this.setSuccess(tryShearBeehive($$2, $$1, $$3) || tryShearEntity($$2, $$3, $$1));
         if (this.isSuccess()) {
            $$1.hurtAndBreak(1, $$2, null, $$0x -> {});
         }
      }

      return $$1;
   }

   private static boolean tryShearBeehive(ServerLevel $$0, ItemStack $$1, net.minecraft.core.BlockPos $$2) {
      BlockState $$3 = $$0.getBlockState($$2);
      if ($$3.is(BlockTags.BEEHIVES, $$0x -> $$0x.hasProperty(BeehiveBlock.HONEY_LEVEL) && $$0x.getBlock() instanceof BeehiveBlock)) {
         int $$4 = (Integer)$$3.getValue(BeehiveBlock.HONEY_LEVEL);
         if ($$4 >= 5) {
            $$0.playSound(null, $$2, SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
            BeehiveBlock.dropHoneycomb($$0, $$1, $$3, $$0.getBlockEntity($$2), null, $$2);
            ((BeehiveBlock)$$3.getBlock()).releaseBeesAndResetHoneyLevel($$0, $$3, $$2, null, BeeReleaseStatus.BEE_RELEASED);
            $$0.gameEvent(null, GameEvent.SHEAR, $$2);
            return true;
         }
      }

      return false;
   }

   private static boolean tryShearEntity(ServerLevel $$0, net.minecraft.core.BlockPos $$1, ItemStack $$2) {
      for (Entity $$4 : $$0.getEntitiesOfClass(Entity.class, new AABB($$1), EntitySelector.NO_SPECTATORS)) {
         if ($$4.shearOffAllLeashConnections(null)) {
            return true;
         }

         if ($$4 instanceof Shearable $$5 && $$5.readyForShearing()) {
            $$5.shear($$0, SoundSource.BLOCKS, $$2);
            $$0.gameEvent(null, GameEvent.SHEAR, $$1);
            return true;
         }
      }

      return false;
   }
}
