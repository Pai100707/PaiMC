package net.minecraft.world.entity.projectile.arrow;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractArrow extends Projectile {
   private static final double ARROW_BASE_DAMAGE = 2.0;
   private static final int SHAKE_TIME = 7;
   private static final float WATER_INERTIA = 0.6F;
   private static final float INERTIA = 0.99F;
   private static final short DEFAULT_LIFE = 0;
   private static final byte DEFAULT_SHAKE = 0;
   private static final boolean DEFAULT_IN_GROUND = false;
   private static final boolean DEFAULT_CRIT = false;
   private static final byte DEFAULT_PIERCE_LEVEL = 0;
   private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> PIERCE_LEVEL = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Boolean> IN_GROUND = SynchedEntityData.defineId(AbstractArrow.class, EntityDataSerializers.BOOLEAN);
   private static final int FLAG_CRIT = 1;
   private static final int FLAG_NOPHYSICS = 2;
   
   private BlockState lastState;
   protected int inGroundTime;
   public AbstractArrow.Pickup pickup = AbstractArrow.Pickup.DISALLOWED;
   public int shakeTime = 0;
   private int life = 0;
   private double baseDamage = 2.0;
   private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
   
   private IntOpenHashSet piercingIgnoreEntityIds;
   
   private List<net.minecraft.world.entity.Entity> piercedAndKilledEntities;
   private ItemStack pickupItemStack = this.getDefaultPickupItem();
   
   private ItemStack firedFromWeapon = null;

   protected AbstractArrow(net.minecraft.world.entity.EntityType<? extends AbstractArrow> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected AbstractArrow(
      net.minecraft.world.entity.EntityType<? extends AbstractArrow> $$0, double $$1, double $$2, double $$3, Level $$4, ItemStack $$5, ItemStack $$6
   ) {
      this($$0, $$4);
      this.pickupItemStack = $$5.copy();
      this.applyComponentsFromItemStack($$5);
      Unit $$7 = (Unit)$$5.remove(DataComponents.INTANGIBLE_PROJECTILE);
      if ($$7 != null) {
         this.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
      }

      this.setPos($$1, $$2, $$3);
      if ($$6 != null && $$4 instanceof ServerLevel $$8) {
         if ($$6.isEmpty()) {
            throw new IllegalArgumentException("Invalid weapon firing an arrow");
         }

         this.firedFromWeapon = $$6.copy();
         int $$9 = EnchantmentHelper.getPiercingCount($$8, $$6, this.pickupItemStack);
         if ($$9 > 0) {
            this.setPierceLevel((byte)$$9);
         }
      }
   }

   protected AbstractArrow(
      net.minecraft.world.entity.EntityType<? extends AbstractArrow> $$0,
      net.minecraft.world.entity.LivingEntity $$1,
      Level $$2,
      ItemStack $$3,
      ItemStack $$4
   ) {
      this($$0, $$1.getX(), $$1.getEyeY() - 0.1F, $$1.getZ(), $$2, $$3, $$4);
      this.setOwner($$1);
   }

   public void setSoundEvent(SoundEvent $$0) {
      this.soundEvent = $$0;
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = this.getBoundingBox().getSize() * 10.0;
      if (Double.isNaN($$1)) {
         $$1 = 1.0;
      }

      $$1 *= 64.0 * getViewScale();
      return $$0 < $$1 * $$1;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(ID_FLAGS, (byte)0);
      $$0.define(PIERCE_LEVEL, (byte)0);
      $$0.define(IN_GROUND, false);
   }

   @Override
   public void shoot(double $$0, double $$1, double $$2, float $$3, float $$4) {
      super.shoot($$0, $$1, $$2, $$3, $$4);
      this.life = 0;
   }

   @Override
   public void lerpMotion(Vec3 $$0) {
      super.lerpMotion($$0);
      this.life = 0;
      if (this.isInGround() && $$0.lengthSqr() > 0.0) {
         this.setInGround(false);
      }
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      super.onSyncedDataUpdated($$0);
      if (!this.firstTick && this.shakeTime <= 0 && $$0.equals(IN_GROUND) && this.isInGround()) {
         this.shakeTime = 7;
      }
   }

   @Override
   public void tick() {
      boolean $$0 = !this.isNoPhysics();
      Vec3 $$1 = this.getDeltaMovement();
      BlockPos $$2 = this.blockPosition();
      BlockState $$3 = this.level().getBlockState($$2);
      if (!$$3.isAir() && $$0) {
         VoxelShape $$4 = $$3.getCollisionShape(this.level(), $$2);
         if (!$$4.isEmpty()) {
            Vec3 $$5 = this.position();

            for (AABB $$6 : $$4.toAabbs()) {
               if ($$6.move($$2).contains($$5)) {
                  this.setDeltaMovement(Vec3.ZERO);
                  this.setInGround(true);
                  break;
               }
            }
         }
      }

      if (this.shakeTime > 0) {
         this.shakeTime--;
      }

      if (this.isInWaterOrRain()) {
         this.clearFire();
      }

      if (this.isInGround() && $$0) {
         if (!this.level().isClientSide()) {
            if (this.lastState != $$3 && this.shouldFall()) {
               this.startFalling();
            } else {
               this.tickDespawn();
            }
         }

         this.inGroundTime++;
         if (this.isAlive()) {
            this.applyEffectsFromBlocks();
         }

         if (!this.level().isClientSide()) {
            this.setSharedFlagOnFire(this.getRemainingFireTicks() > 0);
         }
      } else {
         this.inGroundTime = 0;
         Vec3 $$7 = this.position();
         if (this.isInWater()) {
            this.applyInertia(this.getWaterInertia());
            this.addBubbleParticles($$7);
         }

         if (this.isCritArrow()) {
            for (int $$8 = 0; $$8 < 4; $$8++) {
               this.level()
                  .addParticle(
                     ParticleTypes.CRIT, $$7.x + $$1.x * $$8 / 4.0, $$7.y + $$1.y * $$8 / 4.0, $$7.z + $$1.z * $$8 / 4.0, -$$1.x, -$$1.y + 0.2, -$$1.z
                  );
            }
         }

         float $$9;
         if (!$$0) {
            $$9 = (float)(Mth.atan2(-$$1.x, -$$1.z) * 180.0F / (float)Math.PI);
         } else {
            $$9 = (float)(Mth.atan2($$1.x, $$1.z) * 180.0F / (float)Math.PI);
         }

         float $$11 = (float)(Mth.atan2($$1.y, $$1.horizontalDistance()) * 180.0F / (float)Math.PI);
         this.setXRot(lerpRotation(this.getXRot(), $$11));
         this.setYRot(lerpRotation(this.getYRot(), $$9));
         this.checkLeftOwner();
         if ($$0) {
            BlockHitResult $$12 = this.level().clipIncludingBorder(new ClipContext($$7, $$7.add($$1), Block.COLLIDER, Fluid.NONE, this));
            this.stepMoveAndHit($$12);
         } else {
            this.setPos($$7.add($$1));
            this.applyEffectsFromBlocks();
         }

         if (!this.isInWater()) {
            this.applyInertia(0.99F);
         }

         if ($$0 && !this.isInGround()) {
            this.applyGravity();
         }

         super.tick();
      }
   }

   private void stepMoveAndHit(BlockHitResult $$0) {
      while (this.isAlive()) {
         Vec3 $$1 = this.position();
         ArrayList<EntityHitResult> $$2 = new ArrayList<>(this.findHitEntities($$1, $$0.getLocation()));
         $$2.sort(Comparator.comparingDouble($$1x -> $$1.distanceToSqr($$1x.getEntity().position())));
         EntityHitResult $$3 = $$2.isEmpty() ? null : $$2.getFirst();
         Vec3 $$4 = ((HitResult)Objects.requireNonNullElse($$3, $$0)).getLocation();
         this.setPos($$4);
         this.applyEffectsFromBlocks($$1, $$4);
         if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
            this.handlePortal();
         }

         if ($$2.isEmpty()) {
            if (this.isAlive() && $$0.getType() != Type.MISS) {
               this.hitTargetOrDeflectSelf($$0);
               this.needsSync = true;
            }
            break;
         } else if (this.isAlive() && !this.noPhysics) {
            ProjectileDeflection $$5 = this.hitTargetsOrDeflectSelf($$2);
            this.needsSync = true;
            if (this.getPierceLevel() > 0 && $$5 == ProjectileDeflection.NONE) {
               continue;
            }
            break;
         }
      }
   }

   private ProjectileDeflection hitTargetsOrDeflectSelf(Collection<EntityHitResult> $$0) {
      for (EntityHitResult $$1 : $$0) {
         ProjectileDeflection $$2 = this.hitTargetOrDeflectSelf($$1);
         if (!this.isAlive() || $$2 != ProjectileDeflection.NONE) {
            return $$2;
         }
      }

      return ProjectileDeflection.NONE;
   }

   private void applyInertia(float $$0) {
      Vec3 $$1 = this.getDeltaMovement();
      this.setDeltaMovement($$1.scale($$0));
   }

   private void addBubbleParticles(Vec3 $$0) {
      Vec3 $$1 = this.getDeltaMovement();

      for (int $$2 = 0; $$2 < 4; $$2++) {
         float $$3 = 0.25F;
         this.level().addParticle(ParticleTypes.BUBBLE, $$0.x - $$1.x * 0.25, $$0.y - $$1.y * 0.25, $$0.z - $$1.z * 0.25, $$1.x, $$1.y, $$1.z);
      }
   }

   @Override
   protected double getDefaultGravity() {
      return 0.05;
   }

   private boolean shouldFall() {
      return this.isInGround() && this.level().noCollision(new AABB(this.position(), this.position()).inflate(0.06));
   }

   private void startFalling() {
      this.setInGround(false);
      Vec3 $$0 = this.getDeltaMovement();
      this.setDeltaMovement($$0.multiply(this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F, this.random.nextFloat() * 0.2F));
      this.life = 0;
   }

   protected boolean isInGround() {
      return (Boolean)this.entityData.get(IN_GROUND);
   }

   protected void setInGround(boolean $$0) {
      this.entityData.set(IN_GROUND, $$0);
   }

   @Override
   public boolean isPushedByFluid() {
      return !this.isInGround();
   }

   @Override
   public void move(net.minecraft.world.entity.MoverType $$0, Vec3 $$1) {
      super.move($$0, $$1);
      if ($$0 != net.minecraft.world.entity.MoverType.SELF && this.shouldFall()) {
         this.startFalling();
      }
   }

   protected void tickDespawn() {
      this.life++;
      if (this.life >= 1200) {
         this.discard();
      }
   }

   private void resetPiercedEntities() {
      if (this.piercedAndKilledEntities != null) {
         this.piercedAndKilledEntities.clear();
      }

      if (this.piercingIgnoreEntityIds != null) {
         this.piercingIgnoreEntityIds.clear();
      }
   }

   @Override
   public void onItemBreak(Item $$0) {
      this.firedFromWeapon = null;
   }

   @Override
   public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
      if (!this.isInGround()) {
         super.onAboveBubbleColumn($$0, $$1);
      }
   }

   @Override
   public void onInsideBubbleColumn(boolean $$0) {
      if (!this.isInGround()) {
         super.onInsideBubbleColumn($$0);
      }
   }

   @Override
   public void push(double $$0, double $$1, double $$2) {
      if (!this.isInGround()) {
         super.push($$0, $$1, $$2);
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      super.onHitEntity($$0);
      net.minecraft.world.entity.Entity $$1 = $$0.getEntity();
      float $$2 = (float)this.getDeltaMovement().length();
      double $$3 = this.baseDamage;
      net.minecraft.world.entity.Entity $$4 = this.getOwner();
      DamageSource $$5 = this.damageSources().arrow(this, (net.minecraft.world.entity.Entity)($$4 != null ? $$4 : this));
      if (this.getWeaponItem() != null && this.level() instanceof ServerLevel $$6) {
         $$3 = EnchantmentHelper.modifyDamage($$6, this.getWeaponItem(), $$1, $$5, (float)$$3);
      }

      int $$7 = Mth.ceil(Mth.clamp($$2 * $$3, 0.0, 2.147483647E9));
      if (this.getPierceLevel() > 0) {
         if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
         }

         if (this.piercedAndKilledEntities == null) {
            this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
         }

         if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
            this.discard();
            return;
         }

         this.piercingIgnoreEntityIds.add($$1.getId());
      }

      if (this.isCritArrow()) {
         long $$8 = this.random.nextInt($$7 / 2 + 2);
         $$7 = (int)Math.min($$8 + $$7, 2147483647L);
      }

      if ($$4 instanceof net.minecraft.world.entity.LivingEntity $$9) {
         $$9.setLastHurtMob($$1);
      }

      boolean $$10 = $$1.getType() == net.minecraft.world.entity.EntityType.ENDERMAN;
      int $$11 = $$1.getRemainingFireTicks();
      if (this.isOnFire() && !$$10) {
         $$1.igniteForSeconds(5.0F);
      }

      if ($$1.hurtOrSimulate($$5, $$7)) {
         if ($$10) {
            return;
         }

         if ($$1 instanceof net.minecraft.world.entity.LivingEntity $$12) {
            if (!this.level().isClientSide() && this.getPierceLevel() <= 0) {
               $$12.setArrowCount($$12.getArrowCount() + 1);
            }

            this.doKnockback($$12, $$5);
            if (this.level() instanceof ServerLevel $$13) {
               EnchantmentHelper.doPostAttackEffectsWithItemSource($$13, $$12, $$5, this.getWeaponItem());
            }

            this.doPostHurtEffects($$12);
            if ($$12 instanceof Player && $$4 instanceof ServerPlayer $$14 && !this.isSilent() && $$12 != $$14) {
               $$14.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND, 0.0F));
            }

            if (!$$1.isAlive() && this.piercedAndKilledEntities != null) {
               this.piercedAndKilledEntities.add($$12);
            }

            if (!this.level().isClientSide() && $$4 instanceof ServerPlayer $$15) {
               if (this.piercedAndKilledEntities != null) {
                  CriteriaTriggers.KILLED_BY_ARROW.trigger($$15, this.piercedAndKilledEntities, this.firedFromWeapon);
               } else if (!$$1.isAlive()) {
                  CriteriaTriggers.KILLED_BY_ARROW.trigger($$15, List.of($$1), this.firedFromWeapon);
               }
            }
         }

         this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.discard();
         }
      } else {
         $$1.setRemainingFireTicks($$11);
         this.deflect(ProjectileDeflection.REVERSE, $$1, this.owner, false);
         this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
         if (this.level() instanceof ServerLevel $$16 && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
            if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
               this.spawnAtLocation($$16, this.getPickupItem(), 0.1F);
            }

            this.discard();
         }
      }
   }

   protected void doKnockback(net.minecraft.world.entity.LivingEntity $$0, DamageSource $$1) {
      double $$3 = this.firedFromWeapon != null && this.level() instanceof ServerLevel $$2
         ? EnchantmentHelper.modifyKnockback($$2, this.firedFromWeapon, $$0, $$1, 0.0F)
         : 0.0F;
      if ($$3 > 0.0) {
         double $$4 = Math.max(0.0, 1.0 - $$0.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
         Vec3 $$5 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale($$3 * 0.6 * $$4);
         if ($$5.lengthSqr() > 0.0) {
            $$0.push($$5.x, 0.1, $$5.z);
         }
      }
   }

   @Override
   protected void onHitBlock(BlockHitResult $$0) {
      this.lastState = this.level().getBlockState($$0.getBlockPos());
      super.onHitBlock($$0);
      ItemStack $$1 = this.getWeaponItem();
      if (this.level() instanceof ServerLevel $$2 && $$1 != null) {
         this.hitBlockEnchantmentEffects($$2, $$0, $$1);
      }

      Vec3 $$3 = this.getDeltaMovement();
      Vec3 $$4 = new Vec3(Math.signum($$3.x), Math.signum($$3.y), Math.signum($$3.z));
      Vec3 $$5 = $$4.scale(0.05F);
      this.setPos(this.position().subtract($$5));
      this.setDeltaMovement(Vec3.ZERO);
      this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.setInGround(true);
      this.shakeTime = 7;
      this.setCritArrow(false);
      this.setPierceLevel((byte)0);
      this.setSoundEvent(SoundEvents.ARROW_HIT);
      this.resetPiercedEntities();
   }

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
         $$0x -> this.firedFromWeapon = null
      );
   }

   
   @Override
   public ItemStack getWeaponItem() {
      return this.firedFromWeapon;
   }

   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ARROW_HIT;
   }

   protected final SoundEvent getHitGroundSoundEvent() {
      return this.soundEvent;
   }

   protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity $$0) {
   }

   
   protected EntityHitResult findHitEntity(Vec3 $$0, Vec3 $$1) {
      return ProjectileUtil.getEntityHitResult(
         this.level(), this, $$0, $$1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity
      );
   }

   protected Collection<EntityHitResult> findHitEntities(Vec3 $$0, Vec3 $$1) {
      return ProjectileUtil.getManyEntityHitResult(
         this.level(), this, $$0, $$1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity, false
      );
   }

   @Override
   protected boolean canHitEntity(net.minecraft.world.entity.Entity $$0) {
      return $$0 instanceof Player && this.getOwner() instanceof Player $$1 && !$$1.canHarmPlayer((Player)$$0)
         ? false
         : super.canHitEntity($$0) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains($$0.getId()));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putShort("life", (short)this.life);
      $$0.storeNullable("inBlockState", BlockState.CODEC, this.lastState);
      $$0.putByte("shake", (byte)this.shakeTime);
      $$0.putBoolean("inGround", this.isInGround());
      $$0.store("pickup", AbstractArrow.Pickup.LEGACY_CODEC, this.pickup);
      $$0.putDouble("damage", this.baseDamage);
      $$0.putBoolean("crit", this.isCritArrow());
      $$0.putByte("PierceLevel", this.getPierceLevel());
      $$0.store("SoundEvent", BuiltInRegistries.SOUND_EVENT.byNameCodec(), this.soundEvent);
      $$0.store("item", ItemStack.CODEC, this.pickupItemStack);
      $$0.storeNullable("weapon", ItemStack.CODEC, this.firedFromWeapon);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.life = $$0.getShortOr("life", (short)0);
      this.lastState = (BlockState)$$0.read("inBlockState", BlockState.CODEC).orElse(null);
      this.shakeTime = $$0.getByteOr("shake", (byte)0) & 255;
      this.setInGround($$0.getBooleanOr("inGround", false));
      this.baseDamage = $$0.getDoubleOr("damage", 2.0);
      this.pickup = $$0.read("pickup", AbstractArrow.Pickup.LEGACY_CODEC).orElse(AbstractArrow.Pickup.DISALLOWED);
      this.setCritArrow($$0.getBooleanOr("crit", false));
      this.setPierceLevel($$0.getByteOr("PierceLevel", (byte)0));
      this.soundEvent = $$0.read("SoundEvent", BuiltInRegistries.SOUND_EVENT.byNameCodec()).orElse(this.getDefaultHitGroundSoundEvent());
      this.setPickupItemStack($$0.read("item", ItemStack.CODEC).orElse(this.getDefaultPickupItem()));
      this.firedFromWeapon = (ItemStack)$$0.read("weapon", ItemStack.CODEC).orElse(null);
   }

   @Override
   public void setOwner(net.minecraft.world.entity.Entity $$0) {
      super.setOwner($$0);

      this.pickup = switch ($$0) {
         case Player $$1 when this.pickup == AbstractArrow.Pickup.DISALLOWED -> AbstractArrow.Pickup.ALLOWED;
         case net.minecraft.world.entity.OminousItemSpawner $$2 -> AbstractArrow.Pickup.DISALLOWED;
         case null, default -> this.pickup;
      };
   }

   @Override
   public void playerTouch(Player $$0) {
      if (!this.level().isClientSide() && (this.isInGround() || this.isNoPhysics()) && this.shakeTime <= 0) {
         if (this.tryPickup($$0)) {
            $$0.take(this, 1);
            this.discard();
         }
      }
   }

   protected boolean tryPickup(Player $$0) {
      return switch (this.pickup) {
         case DISALLOWED -> false;
         case ALLOWED -> $$0.getInventory().add(this.getPickupItem());
         case CREATIVE_ONLY -> $$0.hasInfiniteMaterials();
      };
   }

   protected ItemStack getPickupItem() {
      return this.pickupItemStack.copy();
   }

   protected abstract ItemStack getDefaultPickupItem();

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.NONE;
   }

   public ItemStack getPickupItemStackOrigin() {
      return this.pickupItemStack;
   }

   public void setBaseDamage(double $$0) {
      this.baseDamage = $$0;
   }

   @Override
   public boolean isAttackable() {
      return this.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE);
   }

   public void setCritArrow(boolean $$0) {
      this.setFlag(1, $$0);
   }

   private void setPierceLevel(byte $$0) {
      this.entityData.set(PIERCE_LEVEL, $$0);
   }

   private void setFlag(int $$0, boolean $$1) {
      byte $$2 = (Byte)this.entityData.get(ID_FLAGS);
      if ($$1) {
         this.entityData.set(ID_FLAGS, (byte)($$2 | $$0));
      } else {
         this.entityData.set(ID_FLAGS, (byte)($$2 & ~$$0));
      }
   }

   protected void setPickupItemStack(ItemStack $$0) {
      if (!$$0.isEmpty()) {
         this.pickupItemStack = $$0;
      } else {
         this.pickupItemStack = this.getDefaultPickupItem();
      }
   }

   public boolean isCritArrow() {
      byte $$0 = (Byte)this.entityData.get(ID_FLAGS);
      return ($$0 & 1) != 0;
   }

   public byte getPierceLevel() {
      return (Byte)this.entityData.get(PIERCE_LEVEL);
   }

   public void setBaseDamageFromMob(float $$0) {
      this.setBaseDamage($$0 * 2.0F + this.random.triangle(this.level().getDifficulty().getId() * 0.11, 0.57425));
   }

   protected float getWaterInertia() {
      return 0.6F;
   }

   public void setNoPhysics(boolean $$0) {
      this.noPhysics = $$0;
      this.setFlag(2, $$0);
   }

   public boolean isNoPhysics() {
      return !this.level().isClientSide() ? this.noPhysics : ((Byte)this.entityData.get(ID_FLAGS) & 2) != 0;
   }

   @Override
   public boolean isPickable() {
      return super.isPickable() && !this.isInGround();
   }

   
   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      return $$0 == 0 ? net.minecraft.world.entity.SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack) : super.getSlot($$0);
   }

   @Override
   protected boolean shouldBounceOnWorldBorder() {
      return true;
   }

   public static enum Pickup {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static final Codec<AbstractArrow.Pickup> LEGACY_CODEC = Codec.BYTE.xmap(AbstractArrow.Pickup::byOrdinal, $$0 -> (byte)$$0.ordinal());

      public static AbstractArrow.Pickup byOrdinal(int $$0) {
         if ($$0 < 0 || $$0 > values().length) {
            $$0 = 0;
         }

         return values()[$$0];
      }
   }
}
