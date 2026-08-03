package net.minecraft.world.entity.projectile.arrow;

import java.util.Collection;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ThrownTrident extends AbstractArrow {
   private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownTrident.class, EntityDataSerializers.BOOLEAN);
   private static final float WATER_INERTIA = 0.99F;
   private static final boolean DEFAULT_DEALT_DAMAGE = false;
   private boolean dealtDamage = false;
   public int clientSideReturnTridentTickCount;

   public ThrownTrident(net.minecraft.world.entity.EntityType<? extends ThrownTrident> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ThrownTrident(Level $$0, net.minecraft.world.entity.LivingEntity $$1, ItemStack $$2) {
      super(net.minecraft.world.entity.EntityType.TRIDENT, $$1, $$0, $$2, null);
      this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem($$2));
      this.entityData.set(ID_FOIL, $$2.hasFoil());
   }

   public ThrownTrident(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
      super(net.minecraft.world.entity.EntityType.TRIDENT, $$1, $$2, $$3, $$0, $$4, $$4);
      this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem($$4));
      this.entityData.set(ID_FOIL, $$4.hasFoil());
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(ID_LOYALTY, (byte)0);
      $$0.define(ID_FOIL, false);
   }

   @Override
   public void tick() {
      if (this.inGroundTime > 4) {
         this.dealtDamage = true;
      }

      net.minecraft.world.entity.Entity $$0 = this.getOwner();
      int $$1 = (Byte)this.entityData.get(ID_LOYALTY);
      if ($$1 > 0 && (this.dealtDamage || this.isNoPhysics()) && $$0 != null) {
         if (!this.isAcceptibleReturnOwner()) {
            if (this.level() instanceof ServerLevel $$2 && this.pickup == AbstractArrow.Pickup.ALLOWED) {
               this.spawnAtLocation($$2, this.getPickupItem(), 0.1F);
            }

            this.discard();
         } else {
            if (!($$0 instanceof Player) && this.position().distanceTo($$0.getEyePosition()) < $$0.getBbWidth() + 1.0) {
               this.discard();
               return;
            }

            this.setNoPhysics(true);
            Vec3 $$3 = $$0.getEyePosition().subtract(this.position());
            this.setPosRaw(this.getX(), this.getY() + $$3.y * 0.015 * $$1, this.getZ());
            double $$4 = 0.05 * $$1;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add($$3.normalize().scale($$4)));
            if (this.clientSideReturnTridentTickCount == 0) {
               this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
            }

            this.clientSideReturnTridentTickCount++;
         }
      }

      super.tick();
   }

   private boolean isAcceptibleReturnOwner() {
      net.minecraft.world.entity.Entity $$0 = this.getOwner();
      return $$0 == null || !$$0.isAlive() ? false : !($$0 instanceof ServerPlayer) || !$$0.isSpectator();
   }

   public boolean isFoil() {
      return (Boolean)this.entityData.get(ID_FOIL);
   }

   @Nullable
   @Override
   protected EntityHitResult findHitEntity(Vec3 $$0, Vec3 $$1) {
      return this.dealtDamage ? null : super.findHitEntity($$0, $$1);
   }

   @Override
   protected Collection<EntityHitResult> findHitEntities(Vec3 $$0, Vec3 $$1) {
      EntityHitResult $$2 = this.findHitEntity($$0, $$1);
      return $$2 != null ? List.of($$2) : List.of();
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      net.minecraft.world.entity.Entity $$1 = $$0.getEntity();
      float $$2 = 8.0F;
      net.minecraft.world.entity.Entity $$3 = this.getOwner();
      DamageSource $$4 = this.damageSources().trident(this, (net.minecraft.world.entity.Entity)($$3 == null ? this : $$3));
      if (this.level() instanceof ServerLevel $$5) {
         $$2 = EnchantmentHelper.modifyDamage($$5, this.getWeaponItem(), $$1, $$4, $$2);
      }

      this.dealtDamage = true;
      if ($$1.hurtOrSimulate($$4, $$2)) {
         if ($$1.getType() == net.minecraft.world.entity.EntityType.ENDERMAN) {
            return;
         }

         if (this.level() instanceof ServerLevel $$6) {
            EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak($$6, $$1, $$4, this.getWeaponItem(), $$1x -> this.kill($$6));
         }

         if ($$1 instanceof net.minecraft.world.entity.LivingEntity $$7) {
            this.doKnockback($$7, $$4);
            this.doPostHurtEffects($$7);
         }
      }

      this.deflect(ProjectileDeflection.REVERSE, $$1, this.owner, false);
      this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
      this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
   }

   @Override
   protected void hitBlockEnchantmentEffects(ServerLevel $$0, BlockHitResult $$1, ItemStack $$2) {
      Vec3 $$3 = $$1.getBlockPos().clampLocationWithin($$1.getLocation());
      EnchantmentHelper.onHitBlock(
         $$0,
         $$2,
         this.getOwner() instanceof net.minecraft.world.entity.LivingEntity $$4 ? $$4 : null,
         this,
         null,
         $$3,
         $$0.getBlockState($$1.getBlockPos()),
         $$1x -> this.kill($$0)
      );
   }

   @Override
   public ItemStack getWeaponItem() {
      return this.getPickupItemStackOrigin();
   }

   @Override
   protected boolean tryPickup(Player $$0) {
      return super.tryPickup($$0) || this.isNoPhysics() && this.ownedBy($$0) && $$0.getInventory().add(this.getPickupItem());
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(Items.TRIDENT);
   }

   @Override
   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.TRIDENT_HIT_GROUND;
   }

   @Override
   public void playerTouch(Player $$0) {
      if (this.ownedBy($$0) || this.getOwner() == null) {
         super.playerTouch($$0);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.dealtDamage = $$0.getBooleanOr("DealtDamage", false);
      this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(this.getPickupItemStackOrigin()));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("DealtDamage", this.dealtDamage);
   }

   private byte getLoyaltyFromItem(ItemStack $$0) {
      return this.level() instanceof ServerLevel $$1 ? (byte)Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration($$1, $$0, this), 0, 127) : 0;
   }

   @Override
   public void tickDespawn() {
      int $$0 = (Byte)this.entityData.get(ID_LOYALTY);
      if (this.pickup != AbstractArrow.Pickup.ALLOWED || $$0 <= 0) {
         super.tickDespawn();
      }
   }

   @Override
   protected float getWaterInertia() {
      return 0.99F;
   }

   @Override
   public boolean shouldRender(double $$0, double $$1, double $$2) {
      return true;
   }
}
