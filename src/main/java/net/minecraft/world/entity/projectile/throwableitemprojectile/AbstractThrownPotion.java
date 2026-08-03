package net.minecraft.world.entity.projectile.throwableitemprojectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class AbstractThrownPotion extends ThrowableItemProjectile {
   public static final double SPLASH_RANGE = 4.0;
   protected static final double SPLASH_RANGE_SQ = 16.0;
   public static final Predicate<net.minecraft.world.entity.LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = $$0 -> $$0.isSensitiveToWater() || $$0.isOnFire();

   public AbstractThrownPotion(net.minecraft.world.entity.EntityType<? extends AbstractThrownPotion> $$0, Level $$1) {
      super($$0, $$1);
   }

   public AbstractThrownPotion(
      net.minecraft.world.entity.EntityType<? extends AbstractThrownPotion> $$0, Level $$1, net.minecraft.world.entity.LivingEntity $$2, ItemStack $$3
   ) {
      super($$0, $$2, $$1, $$3);
   }

   public AbstractThrownPotion(
      net.minecraft.world.entity.EntityType<? extends AbstractThrownPotion> $$0, Level $$1, double $$2, double $$3, double $$4, ItemStack $$5
   ) {
      super($$0, $$2, $$3, $$4, $$1, $$5);
   }

   @Override
   protected double getDefaultGravity() {
      return 0.05;
   }

   @Override
   protected void onHitBlock(BlockHitResult $$0) {
      super.onHitBlock($$0);
      if (!this.level().isClientSide()) {
         ItemStack $$1 = this.getItem();
         Direction $$2 = $$0.getDirection();
         BlockPos $$3 = $$0.getBlockPos();
         BlockPos $$4 = $$3.relative($$2);
         PotionContents $$5 = (PotionContents)$$1.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         if ($$5.is(Potions.WATER)) {
            this.dowseFire($$4);
            this.dowseFire($$4.relative($$2.getOpposite()));

            for (Direction $$6 : Plane.HORIZONTAL) {
               this.dowseFire($$4.relative($$6));
            }
         }
      }
   }

   @Override
   protected void onHit(HitResult $$0) {
      super.onHit($$0);
      if (this.level() instanceof ServerLevel $$1) {
         ItemStack $$3 = this.getItem();
         PotionContents $$4 = (PotionContents)$$3.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         if ($$4.is(Potions.WATER)) {
            this.onHitAsWater($$1);
         } else if ($$4.hasEffects()) {
            this.onHitAsPotion($$1, $$3, $$0);
         }

         int $$5 = $$4.potion().isPresent() && ((Potion)((Holder)$$4.potion().get()).value()).hasInstantEffects() ? 2007 : 2002;
         $$1.levelEvent($$5, this.blockPosition(), $$4.getColor());
         this.discard();
      }
   }

   private void onHitAsWater(ServerLevel $$0) {
      AABB $$1 = this.getBoundingBox().inflate(4.0, 2.0, 4.0);

      for (net.minecraft.world.entity.LivingEntity $$3 : this.level()
         .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, $$1, WATER_SENSITIVE_OR_ON_FIRE)) {
         double $$4 = this.distanceToSqr($$3);
         if ($$4 < 16.0) {
            if ($$3.isSensitiveToWater()) {
               $$3.hurtServer($$0, this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
            }

            if ($$3.isOnFire() && $$3.isAlive()) {
               $$3.extinguishFire();
            }
         }
      }

      for (Axolotl $$6 : this.level().getEntitiesOfClass(Axolotl.class, $$1)) {
         $$6.rehydrate();
      }
   }

   protected abstract void onHitAsPotion(ServerLevel var1, ItemStack var2, HitResult var3);

   private void dowseFire(BlockPos $$0) {
      BlockState $$1 = this.level().getBlockState($$0);
      if ($$1.is(BlockTags.FIRE)) {
         this.level().destroyBlock($$0, false, this);
      } else if (AbstractCandleBlock.isLit($$1)) {
         AbstractCandleBlock.extinguish(null, $$1, this.level(), $$0);
      } else if (CampfireBlock.isLitCampfire($$1)) {
         this.level().levelEvent(null, 1009, $$0, 0);
         CampfireBlock.dowse(this.getOwner(), this.level(), $$0, $$1);
         this.level().setBlockAndUpdate($$0, (BlockState)$$1.setValue(CampfireBlock.LIT, false));
      }
   }

   @Override
   public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(net.minecraft.world.entity.LivingEntity $$0, DamageSource $$1) {
      double $$2 = $$0.position().x - this.position().x;
      double $$3 = $$0.position().z - this.position().z;
      return DoubleDoubleImmutablePair.of($$2, $$3);
   }
}
