package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class FireChargeItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.ProjectileItem {
   public FireChargeItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      boolean $$4 = false;
      if (!CampfireBlock.canLight($$3) && !CandleBlock.canLight($$3) && !CandleCakeBlock.canLight($$3)) {
         $$2 = $$2.relative($$0.getClickedFace());
         if (BaseFireBlock.canBePlacedAt($$1, $$2, $$0.getHorizontalDirection())) {
            this.playSound($$1, $$2);
            $$1.setBlockAndUpdate($$2, BaseFireBlock.getState($$1, $$2));
            $$1.gameEvent($$0.getPlayer(), GameEvent.BLOCK_PLACE, $$2);
            $$4 = true;
         }
      } else {
         this.playSound($$1, $$2);
         $$1.setBlockAndUpdate($$2, (BlockState)$$3.setValue(BlockStateProperties.LIT, true));
         $$1.gameEvent($$0.getPlayer(), GameEvent.BLOCK_CHANGE, $$2);
         $$4 = true;
      }

      if ($$4) {
         $$0.getItemInHand().shrink(1);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.FAIL;
      }
   }

   private void playSound(Level $$0, BlockPos $$1) {
      RandomSource $$2 = $$0.getRandom();
      $$0.playSound(null, $$1, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, ($$2.nextFloat() - $$2.nextFloat()) * 0.2F + 1.0F);
   }

   @Override
   public Projectile asProjectile(Level $$0, Position $$1, net.minecraft.world.item.ItemStack $$2, Direction $$3) {
      RandomSource $$4 = $$0.getRandom();
      double $$5 = $$4.triangle($$3.getStepX(), 0.11485000000000001);
      double $$6 = $$4.triangle($$3.getStepY(), 0.11485000000000001);
      double $$7 = $$4.triangle($$3.getStepZ(), 0.11485000000000001);
      Vec3 $$8 = new Vec3($$5, $$6, $$7);
      SmallFireball $$9 = new SmallFireball($$0, $$1.x(), $$1.y(), $$1.z(), $$8.normalize());
      $$9.setItem($$2);
      return $$9;
   }

   @Override
   public void shoot(Projectile $$0, double $$1, double $$2, double $$3, float $$4, float $$5) {
   }

   @Override
   public net.minecraft.world.item.ProjectileItem.DispenseConfig createDispenseConfig() {
      return net.minecraft.world.item.ProjectileItem.DispenseConfig.builder()
         .positionFunction(($$0, $$1) -> DispenserBlock.getDispensePosition($$0, 1.0, Vec3.ZERO))
         .uncertainty(6.6666665F)
         .power(1.0F)
         .overrideDispenseEvent(1018)
         .build();
   }
}
