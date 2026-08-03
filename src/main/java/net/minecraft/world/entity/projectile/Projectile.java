package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jspecify.annotations.Nullable;

public abstract class Projectile extends net.minecraft.world.entity.Entity implements net.minecraft.world.entity.TraceableEntity {
   private static final boolean DEFAULT_LEFT_OWNER = false;
   private static final boolean DEFAULT_HAS_BEEN_SHOT = false;
   @Nullable
   protected net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> owner;
   private boolean leftOwner = false;
   private boolean leftOwnerChecked;
   private boolean hasBeenShot = false;
   @Nullable
   private net.minecraft.world.entity.Entity lastDeflectedBy;

   protected Projectile(net.minecraft.world.entity.EntityType<? extends Projectile> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected void setOwner(@Nullable net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> $$0) {
      this.owner = $$0;
   }

   public void setOwner(@Nullable net.minecraft.world.entity.Entity $$0) {
      this.setOwner(net.minecraft.world.entity.EntityReference.of($$0));
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.Entity getOwner() {
      return net.minecraft.world.entity.EntityReference.getEntity(this.owner, this.level());
   }

   public net.minecraft.world.entity.Entity getEffectSource() {
      return (net.minecraft.world.entity.Entity)MoreObjects.firstNonNull(this.getOwner(), this);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      net.minecraft.world.entity.EntityReference.store(this.owner, $$0, "Owner");
      if (this.leftOwner) {
         $$0.putBoolean("LeftOwner", true);
      }

      $$0.putBoolean("HasBeenShot", this.hasBeenShot);
   }

   protected boolean ownedBy(net.minecraft.world.entity.Entity $$0) {
      return this.owner != null && this.owner.matches($$0);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setOwner(net.minecraft.world.entity.EntityReference.read($$0, "Owner"));
      this.leftOwner = $$0.getBooleanOr("LeftOwner", false);
      this.hasBeenShot = $$0.getBooleanOr("HasBeenShot", false);
   }

   @Override
   public void restoreFrom(net.minecraft.world.entity.Entity $$0) {
      super.restoreFrom($$0);
      if ($$0 instanceof Projectile $$1) {
         this.owner = $$1.owner;
      }
   }

   @Override
   public void tick() {
      if (!this.hasBeenShot) {
         this.gameEvent(GameEvent.PROJECTILE_SHOOT, this.getOwner());
         this.hasBeenShot = true;
      }

      this.checkLeftOwner();
      super.tick();
      this.leftOwnerChecked = false;
   }

   protected void checkLeftOwner() {
      if (!this.leftOwner && !this.leftOwnerChecked) {
         this.leftOwner = this.isOutsideOwnerCollisionRange();
         this.leftOwnerChecked = true;
      }
   }

   private boolean isOutsideOwnerCollisionRange() {
      net.minecraft.world.entity.Entity $$0 = this.getOwner();
      if ($$0 != null) {
         AABB $$1 = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0);
         return $$0.getRootVehicle()
            .getSelfAndPassengers()
            .filter(net.minecraft.world.entity.EntitySelector.CAN_BE_PICKED)
            .noneMatch($$1x -> $$1.intersects($$1x.getBoundingBox()));
      } else {
         return true;
      }
   }

   public Vec3 getMovementToShoot(double $$0, double $$1, double $$2, float $$3, float $$4) {
      return new Vec3($$0, $$1, $$2)
         .normalize()
         .add(this.random.triangle(0.0, 0.0172275 * $$4), this.random.triangle(0.0, 0.0172275 * $$4), this.random.triangle(0.0, 0.0172275 * $$4))
         .scale($$3);
   }

   public void shoot(double $$0, double $$1, double $$2, float $$3, float $$4) {
      Vec3 $$5 = this.getMovementToShoot($$0, $$1, $$2, $$3, $$4);
      this.setDeltaMovement($$5);
      this.needsSync = true;
      double $$6 = $$5.horizontalDistance();
      this.setYRot((float)(Mth.atan2($$5.x, $$5.z) * 180.0F / (float)Math.PI));
      this.setXRot((float)(Mth.atan2($$5.y, $$6) * 180.0F / (float)Math.PI));
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public void shootFromRotation(net.minecraft.world.entity.Entity $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
      float $$6 = -Mth.sin($$2 * (float) (Math.PI / 180.0)) * Mth.cos($$1 * (float) (Math.PI / 180.0));
      float $$7 = -Mth.sin(($$1 + $$3) * (float) (Math.PI / 180.0));
      float $$8 = Mth.cos($$2 * (float) (Math.PI / 180.0)) * Mth.cos($$1 * (float) (Math.PI / 180.0));
      this.shoot($$6, $$7, $$8, $$4, $$5);
      Vec3 $$9 = $$0.getKnownMovement();
      this.setDeltaMovement(this.getDeltaMovement().add($$9.x, $$0.onGround() ? 0.0 : $$9.y, $$9.z));
   }

   @Override
   public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
      double $$2 = $$0 ? -0.03 : 0.1;
      this.setDeltaMovement(this.getDeltaMovement().add(0.0, $$2, 0.0));
      sendBubbleColumnParticles(this.level(), $$1);
   }

   @Override
   public void onInsideBubbleColumn(boolean $$0) {
      double $$1 = $$0 ? -0.03 : 0.06;
      this.setDeltaMovement(this.getDeltaMovement().add(0.0, $$1, 0.0));
      this.resetFallDistance();
   }

   public static <T extends Projectile> T spawnProjectileFromRotation(
      Projectile.ProjectileFactory<T> $$0, ServerLevel $$1, ItemStack $$2, net.minecraft.world.entity.LivingEntity $$3, float $$4, float $$5, float $$6
   ) {
      return spawnProjectile($$0.create($$1, $$3, $$2), $$1, $$2, $$4x -> $$4x.shootFromRotation($$3, $$3.getXRot(), $$3.getYRot(), $$4, $$5, $$6));
   }

   public static <T extends Projectile> T spawnProjectileUsingShoot(
      Projectile.ProjectileFactory<T> $$0,
      ServerLevel $$1,
      ItemStack $$2,
      net.minecraft.world.entity.LivingEntity $$3,
      double $$4,
      double $$5,
      double $$6,
      float $$7,
      float $$8
   ) {
      return spawnProjectile($$0.create($$1, $$3, $$2), $$1, $$2, $$5x -> $$5x.shoot($$4, $$5, $$6, $$7, $$8));
   }

   public static <T extends Projectile> T spawnProjectileUsingShoot(
      T $$0, ServerLevel $$1, ItemStack $$2, double $$3, double $$4, double $$5, float $$6, float $$7
   ) {
      return spawnProjectile($$0, $$1, $$2, $$6x -> $$0.shoot($$3, $$4, $$5, $$6, $$7));
   }

   public static <T extends Projectile> T spawnProjectile(T $$0, ServerLevel $$1, ItemStack $$2) {
      return spawnProjectile($$0, $$1, $$2, $$0x -> {});
   }

   public static <T extends Projectile> T spawnProjectile(T $$0, ServerLevel $$1, ItemStack $$2, Consumer<T> $$3) {
      $$3.accept($$0);
      $$1.addFreshEntity($$0);
      $$0.applyOnProjectileSpawned($$1, $$2);
      return $$0;
   }

   public void applyOnProjectileSpawned(ServerLevel $$0, ItemStack $$1) {
      EnchantmentHelper.onProjectileSpawned($$0, $$1, this, $$0x -> {});
      if (this instanceof AbstractArrow $$2) {
         ItemStack $$3 = $$2.getWeaponItem();
         if ($$3 != null && !$$3.isEmpty() && !$$1.getItem().equals($$3.getItem())) {
            EnchantmentHelper.onProjectileSpawned($$0, $$3, this, $$2::onItemBreak);
         }
      }
   }

   protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult $$0) {
      if ($$0.getType() == Type.ENTITY) {
         EntityHitResult $$1 = (EntityHitResult)$$0;
         net.minecraft.world.entity.Entity $$2 = $$1.getEntity();
         ProjectileDeflection $$3 = $$2.deflection(this);
         if ($$3 != ProjectileDeflection.NONE) {
            if ($$2 != this.lastDeflectedBy && this.deflect($$3, $$2, this.owner, false)) {
               this.lastDeflectedBy = $$2;
            }

            return $$3;
         }
      } else if (this.shouldBounceOnWorldBorder() && $$0 instanceof BlockHitResult $$4 && $$4.isWorldBorderHit()) {
         ProjectileDeflection $$5 = ProjectileDeflection.REVERSE;
         if (this.deflect($$5, null, this.owner, false)) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            return $$5;
         }
      }

      this.onHit($$0);
      return ProjectileDeflection.NONE;
   }

   protected boolean shouldBounceOnWorldBorder() {
      return false;
   }

   public boolean deflect(
      ProjectileDeflection $$0,
      @Nullable net.minecraft.world.entity.Entity $$1,
      @Nullable net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.Entity> $$2,
      boolean $$3
   ) {
      $$0.deflect(this, $$1, this.random);
      if (!this.level().isClientSide()) {
         this.setOwner($$2);
         this.onDeflection($$3);
      }

      return true;
   }

   protected void onDeflection(boolean $$0) {
   }

   protected void onItemBreak(Item $$0) {
   }

   protected void onHit(HitResult $$0) {
      Type $$1 = $$0.getType();
      if ($$1 == Type.ENTITY) {
         EntityHitResult $$2 = (EntityHitResult)$$0;
         net.minecraft.world.entity.Entity $$3 = $$2.getEntity();
         if ($$3.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && $$3 instanceof Projectile $$4) {
            $$4.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.owner, true);
         }

         this.onHitEntity($$2);
         this.level().gameEvent(GameEvent.PROJECTILE_LAND, $$0.getLocation(), Context.of(this, null));
      } else if ($$1 == Type.BLOCK) {
         BlockHitResult $$5 = (BlockHitResult)$$0;
         this.onHitBlock($$5);
         BlockPos $$6 = $$5.getBlockPos();
         this.level().gameEvent(GameEvent.PROJECTILE_LAND, $$6, Context.of(this, this.level().getBlockState($$6)));
      }
   }

   protected void onHitEntity(EntityHitResult $$0) {
   }

   protected void onHitBlock(BlockHitResult $$0) {
      BlockState $$1 = this.level().getBlockState($$0.getBlockPos());
      $$1.onProjectileHit(this.level(), $$1, $$0, this);
   }

   protected boolean canHitEntity(net.minecraft.world.entity.Entity $$0) {
      if (!$$0.canBeHitByProjectile()) {
         return false;
      } else {
         net.minecraft.world.entity.Entity $$1 = this.getOwner();
         return $$1 == null || this.leftOwner || !$$1.isPassengerOfSameVehicle($$0);
      }
   }

   protected void updateRotation() {
      Vec3 $$0 = this.getDeltaMovement();
      double $$1 = $$0.horizontalDistance();
      this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2($$0.y, $$1) * 180.0F / (float)Math.PI)));
      this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2($$0.x, $$0.z) * 180.0F / (float)Math.PI)));
   }

   protected static float lerpRotation(float $$0, float $$1) {
      while ($$1 - $$0 < -180.0F) {
         $$0 -= 360.0F;
      }

      while ($$1 - $$0 >= 180.0F) {
         $$0 += 360.0F;
      }

      return Mth.lerp(0.2F, $$0, $$1);
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      net.minecraft.world.entity.Entity $$1 = this.getOwner();
      return new ClientboundAddEntityPacket(this, $$0, $$1 == null ? 0 : $$1.getId());
   }

   @Override
   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      super.recreateFromPacket($$0);
      net.minecraft.world.entity.Entity $$1 = this.level().getEntity($$0.getData());
      if ($$1 != null) {
         this.setOwner($$1);
      }
   }

   @Override
   public boolean mayInteract(ServerLevel $$0, BlockPos $$1) {
      net.minecraft.world.entity.Entity $$2 = this.getOwner();
      return $$2 instanceof Player ? $$2.mayInteract($$0, $$1) : $$2 == null || (Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING);
   }

   public boolean mayBreak(ServerLevel $$0) {
      return this.getType().is(EntityTypeTags.IMPACT_PROJECTILES) && (Boolean)$$0.getGameRules().get(GameRules.PROJECTILES_CAN_BREAK_BLOCKS);
   }

   @Override
   public boolean isPickable() {
      return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
   }

   @Override
   public float getPickRadius() {
      return this.isPickable() ? 1.0F : 0.0F;
   }

   public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(net.minecraft.world.entity.LivingEntity $$0, DamageSource $$1) {
      double $$2 = this.getDeltaMovement().x;
      double $$3 = this.getDeltaMovement().z;
      return DoubleDoubleImmutablePair.of($$2, $$3);
   }

   @Override
   public int getDimensionChangingDelay() {
      return 2;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (!this.isInvulnerableToBase($$1)) {
         this.markHurt();
      }

      return false;
   }

   @FunctionalInterface
   public interface ProjectileFactory<T extends Projectile> {
      T create(ServerLevel var1, net.minecraft.world.entity.LivingEntity var2, ItemStack var3);
   }
}
