package net.minecraft.world.entity.projectile.hurtingprojectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LargeFireball extends Fireball {
   private static final byte DEFAULT_EXPLOSION_POWER = 1;
   private int explosionPower = 1;

   public LargeFireball(net.minecraft.world.entity.EntityType<? extends LargeFireball> $$0, Level $$1) {
      super($$0, $$1);
   }

   public LargeFireball(Level $$0, net.minecraft.world.entity.LivingEntity $$1, Vec3 $$2, int $$3) {
      super(net.minecraft.world.entity.EntityType.FIREBALL, $$1, $$2, $$0);
      this.explosionPower = $$3;
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);
      if (this.level() instanceof ServerLevel $$1) {
         boolean $$2 = (Boolean)$$1.getGameRules().get(GameRules.MOB_GRIEFING);
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionPower, $$2, ExplosionInteraction.MOB);
         this.discard();
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      super.onHitEntity($$0);
      if (this.level() instanceof ServerLevel $$1) {
         net.minecraft.world.entity.Entity var6 = $$0.getEntity();
         net.minecraft.world.entity.Entity $$4 = this.getOwner();
         DamageSource $$5 = this.damageSources().fireball(this, $$4);
         var6.hurtServer($$1, $$5, 6.0F);
         EnchantmentHelper.doPostAttackEffects($$1, var6, $$5);
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putByte("ExplosionPower", (byte)this.explosionPower);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.explosionPower = $$0.getByteOr("ExplosionPower", (byte)1);
   }
}
