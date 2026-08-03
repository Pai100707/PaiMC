package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class WindChargeItem extends net.minecraft.world.item.Item implements net.minecraft.world.item.ProjectileItem {
   public static float PROJECTILE_SHOOT_POWER = 1.5F;

   public WindChargeItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
      net.minecraft.world.item.ItemStack $$3 = $$1.getItemInHand($$2);
      if ($$0 instanceof ServerLevel $$4) {
         Projectile.spawnProjectileFromRotation(
            ($$2x, $$3x, $$4x) -> new WindCharge($$1, $$0, $$1.position().x(), $$1.getEyePosition().y(), $$1.position().z()),
            $$4,
            $$3,
            $$1,
            0.0F,
            PROJECTILE_SHOOT_POWER,
            1.0F
         );
      }

      $$0.playSound(
         null, $$1.getX(), $$1.getY(), $$1.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / ($$0.getRandom().nextFloat() * 0.4F + 0.8F)
      );
      $$1.awardStat(Stats.ITEM_USED.get(this));
      $$3.consume(1, $$1);
      return InteractionResult.SUCCESS;
   }

   @Override
   public Projectile asProjectile(Level $$0, Position $$1, net.minecraft.world.item.ItemStack $$2, Direction $$3) {
      RandomSource $$4 = $$0.getRandom();
      double $$5 = $$4.triangle($$3.getStepX(), 0.11485000000000001);
      double $$6 = $$4.triangle($$3.getStepY(), 0.11485000000000001);
      double $$7 = $$4.triangle($$3.getStepZ(), 0.11485000000000001);
      Vec3 $$8 = new Vec3($$5, $$6, $$7);
      WindCharge $$9 = new WindCharge($$0, $$1.x(), $$1.y(), $$1.z(), $$8);
      $$9.setDeltaMovement($$8);
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
         .overrideDispenseEvent(1051)
         .build();
   }
}
