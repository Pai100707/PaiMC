package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EyeOfEnder extends net.minecraft.world.entity.Entity implements ItemSupplier {
   private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
   private static final float TOO_FAR_SIGNAL_HEIGHT = 8.0F;
   private static final float TOO_FAR_DISTANCE = 12.0F;
   private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);
   @Nullable
   private Vec3 target;
   private int life;
   private boolean surviveAfterDeath;

   public EyeOfEnder(net.minecraft.world.entity.EntityType<? extends EyeOfEnder> $$0, Level $$1) {
      super($$0, $$1);
   }

   public EyeOfEnder(Level $$0, double $$1, double $$2, double $$3) {
      this(net.minecraft.world.entity.EntityType.EYE_OF_ENDER, $$0);
      this.setPos($$1, $$2, $$3);
   }

   public void setItem(ItemStack $$0) {
      if ($$0.isEmpty()) {
         this.getEntityData().set(DATA_ITEM_STACK, this.getDefaultItem());
      } else {
         this.getEntityData().set(DATA_ITEM_STACK, $$0.copyWithCount(1));
      }
   }

   @Override
   public ItemStack getItem() {
      return (ItemStack)this.getEntityData().get(DATA_ITEM_STACK);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_ITEM_STACK, this.getDefaultItem());
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      if (this.tickCount < 2 && $$0 < 12.25) {
         return false;
      } else {
         double $$1 = this.getBoundingBox().getSize() * 4.0;
         if (Double.isNaN($$1)) {
            $$1 = 4.0;
         }

         $$1 *= 64.0;
         return $$0 < $$1 * $$1;
      }
   }

   public void signalTo(Vec3 $$0) {
      Vec3 $$1 = $$0.subtract(this.position());
      double $$2 = $$1.horizontalDistance();
      if ($$2 > 12.0) {
         this.target = this.position().add($$1.x / $$2 * 12.0, 8.0, $$1.z / $$2 * 12.0);
      } else {
         this.target = $$0;
      }

      this.life = 0;
      this.surviveAfterDeath = this.random.nextInt(5) > 0;
   }

   @Override
   public void tick() {
      super.tick();
      Vec3 $$0 = this.position().add(this.getDeltaMovement());
      if (!this.level().isClientSide() && this.target != null) {
         this.setDeltaMovement(updateDeltaMovement(this.getDeltaMovement(), $$0, this.target));
      }

      if (this.level().isClientSide()) {
         Vec3 $$1 = $$0.subtract(this.getDeltaMovement().scale(0.25));
         this.spawnParticles($$1, this.getDeltaMovement());
      }

      this.setPos($$0);
      if (!this.level().isClientSide()) {
         this.life++;
         if (this.life > 80 && !this.level().isClientSide()) {
            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.discard();
            if (this.surviveAfterDeath) {
               this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
            } else {
               this.level().levelEvent(2003, this.blockPosition(), 0);
            }
         }
      }
   }

   private void spawnParticles(Vec3 $$0, Vec3 $$1) {
      if (this.isInWater()) {
         for (int $$2 = 0; $$2 < 4; $$2++) {
            this.level().addParticle(ParticleTypes.BUBBLE, $$0.x, $$0.y, $$0.z, $$1.x, $$1.y, $$1.z);
         }
      } else {
         this.level()
            .addParticle(
               ParticleTypes.PORTAL,
               $$0.x + this.random.nextDouble() * 0.6 - 0.3,
               $$0.y - 0.5,
               $$0.z + this.random.nextDouble() * 0.6 - 0.3,
               $$1.x,
               $$1.y,
               $$1.z
            );
      }
   }

   private static Vec3 updateDeltaMovement(Vec3 $$0, Vec3 $$1, Vec3 $$2) {
      Vec3 $$3 = new Vec3($$2.x - $$1.x, 0.0, $$2.z - $$1.z);
      double $$4 = $$3.length();
      double $$5 = Mth.lerp(0.0025, $$0.horizontalDistance(), $$4);
      double $$6 = $$0.y;
      if ($$4 < 1.0) {
         $$5 *= 0.8;
         $$6 *= 0.8;
      }

      double $$7 = $$1.y - $$0.y < $$2.y ? 1.0 : -1.0;
      return $$3.scale($$5 / $$4).add(0.0, $$6 + ($$7 - $$6) * 0.015, 0.0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.store("Item", ItemStack.CODEC, this.getItem());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setItem($$0.read("Item", ItemStack.CODEC).orElse(this.getDefaultItem()));
   }

   private ItemStack getDefaultItem() {
      return new ItemStack(Items.ENDER_EYE);
   }

   @Override
   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   @Override
   public boolean isAttackable() {
      return false;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return false;
   }
}
