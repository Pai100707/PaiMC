package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ArmorStand extends net.minecraft.world.entity.LivingEntity {
   public static final int WOBBLE_TIME = 5;
   private static final boolean ENABLE_ARMS = true;
   public static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   public static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   public static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
   public static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
   public static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0F, 0.0F, -1.0F);
   public static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0F, 0.0F, 1.0F);
   private static final net.minecraft.world.entity.EntityDimensions MARKER_DIMENSIONS = net.minecraft.world.entity.EntityDimensions.fixed(0.0F, 0.0F);
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.ARMOR_STAND
      .getDimensions()
      .scale(0.5F)
      .withEyeHeight(0.9875F);
   private static final double FEET_OFFSET = 0.1;
   private static final double CHEST_OFFSET = 0.9;
   private static final double LEGS_OFFSET = 0.4;
   private static final double HEAD_OFFSET = 1.6;
   public static final int DISABLE_TAKING_OFFSET = 8;
   public static final int DISABLE_PUTTING_OFFSET = 16;
   public static final int CLIENT_FLAG_SMALL = 1;
   public static final int CLIENT_FLAG_SHOW_ARMS = 4;
   public static final int CLIENT_FLAG_NO_BASEPLATE = 8;
   public static final int CLIENT_FLAG_MARKER = 16;
   public static final EntityDataAccessor<Byte> DATA_CLIENT_FLAGS = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Rotations> DATA_HEAD_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_BODY_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_LEFT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_RIGHT_ARM_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_LEFT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   public static final EntityDataAccessor<Rotations> DATA_RIGHT_LEG_POSE = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.ROTATIONS);
   private static final Predicate<net.minecraft.world.entity.Entity> RIDABLE_MINECARTS = $$0 -> $$0 instanceof AbstractMinecart $$1 && $$1.isRideable();
   private static final boolean DEFAULT_INVISIBLE = false;
   private static final int DEFAULT_DISABLED_SLOTS = 0;
   private static final boolean DEFAULT_SMALL = false;
   private static final boolean DEFAULT_SHOW_ARMS = false;
   private static final boolean DEFAULT_NO_BASE_PLATE = false;
   private static final boolean DEFAULT_MARKER = false;
   private boolean invisible = false;
   public long lastHit;
   private int disabledSlots = 0;

   public ArmorStand(net.minecraft.world.entity.EntityType<? extends ArmorStand> $$0, Level $$1) {
      super($$0, $$1);
   }

   public ArmorStand(Level $$0, double $$1, double $$2, double $$3) {
      this(net.minecraft.world.entity.EntityType.ARMOR_STAND, $$0);
      this.setPos($$1, $$2, $$3);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createLivingAttributes().add(Attributes.STEP_HEIGHT, 0.0);
   }

   @Override
   public void refreshDimensions() {
      double $$0 = this.getX();
      double $$1 = this.getY();
      double $$2 = this.getZ();
      super.refreshDimensions();
      this.setPos($$0, $$1, $$2);
   }

   private boolean hasPhysics() {
      return !this.isMarker() && !this.isNoGravity();
   }

   @Override
   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && this.hasPhysics();
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_CLIENT_FLAGS, (byte)0);
      $$0.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
      $$0.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
      $$0.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
      $$0.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
      $$0.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
      $$0.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
   }

   @Override
   public boolean canUseSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 != net.minecraft.world.entity.EquipmentSlot.BODY && $$0 != net.minecraft.world.entity.EquipmentSlot.SADDLE && !this.isDisabled($$0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("Invisible", this.isInvisible());
      $$0.putBoolean("Small", this.isSmall());
      $$0.putBoolean("ShowArms", this.showArms());
      $$0.putInt("DisabledSlots", this.disabledSlots);
      $$0.putBoolean("NoBasePlate", !this.showBasePlate());
      if (this.isMarker()) {
         $$0.putBoolean("Marker", this.isMarker());
      }

      $$0.store("Pose", ArmorStand.ArmorStandPose.CODEC, this.getArmorStandPose());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setInvisible($$0.getBooleanOr("Invisible", false));
      this.setSmall($$0.getBooleanOr("Small", false));
      this.setShowArms($$0.getBooleanOr("ShowArms", false));
      this.disabledSlots = $$0.getIntOr("DisabledSlots", 0);
      this.setNoBasePlate($$0.getBooleanOr("NoBasePlate", false));
      this.setMarker($$0.getBooleanOr("Marker", false));
      this.noPhysics = !this.hasPhysics();
      $$0.read("Pose", ArmorStand.ArmorStandPose.CODEC).ifPresent(this::setArmorStandPose);
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(net.minecraft.world.entity.Entity $$0) {
   }

   @Override
   protected void pushEntities() {
      for (net.minecraft.world.entity.Entity $$1 : this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS)) {
         if (this.distanceToSqr($$1) <= 0.2) {
            $$1.push(this);
         }
      }
   }

   @Override
   public InteractionResult interactAt(Player $$0, Vec3 $$1, InteractionHand $$2) {
      ItemStack $$3 = $$0.getItemInHand($$2);
      if (this.isMarker() || $$3.is(Items.NAME_TAG)) {
         return InteractionResult.PASS;
      } else if ($$0.isSpectator()) {
         return InteractionResult.SUCCESS;
      } else if ($$0.level().isClientSide()) {
         return InteractionResult.SUCCESS_SERVER;
      } else {
         net.minecraft.world.entity.EquipmentSlot $$4 = this.getEquipmentSlotForItem($$3);
         if ($$3.isEmpty()) {
            net.minecraft.world.entity.EquipmentSlot $$5 = this.getClickedSlot($$1);
            net.minecraft.world.entity.EquipmentSlot $$6 = this.isDisabled($$5) ? $$4 : $$5;
            if (this.hasItemInSlot($$6) && this.swapItem($$0, $$6, $$3, $$2)) {
               return InteractionResult.SUCCESS_SERVER;
            }
         } else {
            if (this.isDisabled($$4)) {
               return InteractionResult.FAIL;
            }

            if ($$4.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HAND && !this.showArms()) {
               return InteractionResult.FAIL;
            }

            if (this.swapItem($$0, $$4, $$3, $$2)) {
               return InteractionResult.SUCCESS_SERVER;
            }
         }

         return InteractionResult.PASS;
      }
   }

   private net.minecraft.world.entity.EquipmentSlot getClickedSlot(Vec3 $$0) {
      net.minecraft.world.entity.EquipmentSlot $$1 = net.minecraft.world.entity.EquipmentSlot.MAINHAND;
      boolean $$2 = this.isSmall();
      double $$3 = $$0.y / (this.getScale() * this.getAgeScale());
      net.minecraft.world.entity.EquipmentSlot $$4 = net.minecraft.world.entity.EquipmentSlot.FEET;
      if ($$3 >= 0.1 && $$3 < 0.1 + ($$2 ? 0.8 : 0.45) && this.hasItemInSlot($$4)) {
         $$1 = net.minecraft.world.entity.EquipmentSlot.FEET;
      } else if ($$3 >= 0.9 + ($$2 ? 0.3 : 0.0) && $$3 < 0.9 + ($$2 ? 1.0 : 0.7) && this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.CHEST)) {
         $$1 = net.minecraft.world.entity.EquipmentSlot.CHEST;
      } else if ($$3 >= 0.4 && $$3 < 0.4 + ($$2 ? 1.0 : 0.8) && this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.LEGS)) {
         $$1 = net.minecraft.world.entity.EquipmentSlot.LEGS;
      } else if ($$3 >= 1.6 && this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.HEAD)) {
         $$1 = net.minecraft.world.entity.EquipmentSlot.HEAD;
      } else if (!this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND) && this.hasItemInSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND)
         )
       {
         $$1 = net.minecraft.world.entity.EquipmentSlot.OFFHAND;
      }

      return $$1;
   }

   private boolean isDisabled(net.minecraft.world.entity.EquipmentSlot $$0) {
      return (this.disabledSlots & 1 << $$0.getFilterBit(0)) != 0 || $$0.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HAND && !this.showArms();
   }

   private boolean swapItem(Player $$0, net.minecraft.world.entity.EquipmentSlot $$1, ItemStack $$2, InteractionHand $$3) {
      ItemStack $$4 = this.getItemBySlot($$1);
      if (!$$4.isEmpty() && (this.disabledSlots & 1 << $$1.getFilterBit(8)) != 0) {
         return false;
      } else if ($$4.isEmpty() && (this.disabledSlots & 1 << $$1.getFilterBit(16)) != 0) {
         return false;
      } else if ($$0.hasInfiniteMaterials() && $$4.isEmpty() && !$$2.isEmpty()) {
         this.setItemSlot($$1, $$2.copyWithCount(1));
         return true;
      } else if ($$2.isEmpty() || $$2.getCount() <= 1) {
         this.setItemSlot($$1, $$2);
         $$0.setItemInHand($$3, $$4);
         return true;
      } else if (!$$4.isEmpty()) {
         return false;
      } else {
         this.setItemSlot($$1, $$2.split(1));
         return true;
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isRemoved()) {
         return false;
      } else if (!(Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING) && $$1.getEntity() instanceof net.minecraft.world.entity.Mob) {
         return false;
      } else if ($$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         this.kill($$0);
         return false;
      } else if (this.isInvulnerableTo($$0, $$1) || this.invisible || this.isMarker()) {
         return false;
      } else if ($$1.is(DamageTypeTags.IS_EXPLOSION)) {
         this.brokenByAnything($$0, $$1);
         this.kill($$0);
         return false;
      } else if ($$1.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
         if (this.isOnFire()) {
            this.causeDamage($$0, $$1, 0.15F);
         } else {
            this.igniteForSeconds(5.0F);
         }

         return false;
      } else if ($$1.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
         this.causeDamage($$0, $$1, 4.0F);
         return false;
      } else {
         boolean $$3 = $$1.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
         boolean $$4 = $$1.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
         if (!$$3 && !$$4) {
            return false;
         } else if ($$1.getEntity() instanceof Player $$5 && !$$5.getAbilities().mayBuild) {
            return false;
         } else if ($$1.isCreativePlayer()) {
            this.playBrokenSound();
            this.showBreakingParticles();
            this.kill($$0);
            return true;
         } else {
            long $$6 = $$0.getGameTime();
            if ($$6 - this.lastHit > 5L && !$$4) {
               $$0.broadcastEntityEvent(this, (byte)32);
               this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
               this.lastHit = $$6;
            } else {
               this.brokenByPlayer($$0, $$1);
               this.showBreakingParticles();
               this.kill($$0);
            }

            return true;
         }
      }
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 32) {
         if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
            this.lastHit = this.level().getGameTime();
         }
      } else {
         super.handleEntityEvent($$0);
      }
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = this.getBoundingBox().getSize() * 4.0;
      if (Double.isNaN($$1) || $$1 == 0.0) {
         $$1 = 4.0;
      }

      $$1 *= 64.0;
      return $$0 < $$1 * $$1;
   }

   private void showBreakingParticles() {
      if (this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level())
            .sendParticles(
               new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
               this.getX(),
               this.getY(0.6666666666666666),
               this.getZ(),
               10,
               this.getBbWidth() / 4.0F,
               this.getBbHeight() / 4.0F,
               this.getBbWidth() / 4.0F,
               0.05
            );
      }
   }

   private void causeDamage(ServerLevel $$0, DamageSource $$1, float $$2) {
      float $$3 = this.getHealth();
      $$3 -= $$2;
      if ($$3 <= 0.5F) {
         this.brokenByAnything($$0, $$1);
         this.kill($$0);
      } else {
         this.setHealth($$3);
         this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
      }
   }

   private void brokenByPlayer(ServerLevel $$0, DamageSource $$1) {
      ItemStack $$2 = new ItemStack(Items.ARMOR_STAND);
      $$2.set(DataComponents.CUSTOM_NAME, this.getCustomName());
      Block.popResource(this.level(), this.blockPosition(), $$2);
      this.brokenByAnything($$0, $$1);
   }

   private void brokenByAnything(ServerLevel $$0, DamageSource $$1) {
      this.playBrokenSound();
      this.dropAllDeathLoot($$0, $$1);

      for (net.minecraft.world.entity.EquipmentSlot $$2 : net.minecraft.world.entity.EquipmentSlot.VALUES) {
         ItemStack $$3 = this.equipment.set($$2, ItemStack.EMPTY);
         if (!$$3.isEmpty()) {
            Block.popResource(this.level(), this.blockPosition().above(), $$3);
         }
      }
   }

   private void playBrokenSound() {
      this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
   }

   @Override
   protected void tickHeadTurn(float $$0) {
      this.yBodyRotO = this.yRotO;
      this.yBodyRot = this.getYRot();
   }

   @Override
   public void travel(Vec3 $$0) {
      if (this.hasPhysics()) {
         super.travel($$0);
      }
   }

   @Override
   public void setYBodyRot(float $$0) {
      this.yBodyRotO = this.yRotO = $$0;
      this.yHeadRotO = this.yHeadRot = $$0;
   }

   @Override
   public void setYHeadRot(float $$0) {
      this.yBodyRotO = this.yRotO = $$0;
      this.yHeadRotO = this.yHeadRot = $$0;
   }

   @Override
   protected void updateInvisibilityStatus() {
      this.setInvisible(this.invisible);
   }

   @Override
   public void setInvisible(boolean $$0) {
      this.invisible = $$0;
      super.setInvisible($$0);
   }

   @Override
   public boolean isBaby() {
      return this.isSmall();
   }

   @Override
   public void kill(ServerLevel $$0) {
      this.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
   }

   @Override
   public boolean ignoreExplosion(Explosion $$0) {
      return $$0.shouldAffectBlocklikeEntities() ? this.isInvisible() : true;
   }

   @Override
   public PushReaction getPistonPushReaction() {
      return this.isMarker() ? PushReaction.IGNORE : super.getPistonPushReaction();
   }

   @Override
   public boolean isIgnoringBlockTriggers() {
      return this.isMarker();
   }

   private void setSmall(boolean $$0) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit((Byte)this.entityData.get(DATA_CLIENT_FLAGS), 1, $$0));
   }

   public boolean isSmall() {
      return ((Byte)this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
   }

   public void setShowArms(boolean $$0) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit((Byte)this.entityData.get(DATA_CLIENT_FLAGS), 4, $$0));
   }

   public boolean showArms() {
      return ((Byte)this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
   }

   public void setNoBasePlate(boolean $$0) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit((Byte)this.entityData.get(DATA_CLIENT_FLAGS), 8, $$0));
   }

   public boolean showBasePlate() {
      return ((Byte)this.entityData.get(DATA_CLIENT_FLAGS) & 8) == 0;
   }

   private void setMarker(boolean $$0) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit((Byte)this.entityData.get(DATA_CLIENT_FLAGS), 16, $$0));
   }

   public boolean isMarker() {
      return ((Byte)this.entityData.get(DATA_CLIENT_FLAGS) & 16) != 0;
   }

   private byte setBit(byte $$0, int $$1, boolean $$2) {
      if ($$2) {
         $$0 = (byte)($$0 | $$1);
      } else {
         $$0 = (byte)($$0 & ~$$1);
      }

      return $$0;
   }

   public void setHeadPose(Rotations $$0) {
      this.entityData.set(DATA_HEAD_POSE, $$0);
   }

   public void setBodyPose(Rotations $$0) {
      this.entityData.set(DATA_BODY_POSE, $$0);
   }

   public void setLeftArmPose(Rotations $$0) {
      this.entityData.set(DATA_LEFT_ARM_POSE, $$0);
   }

   public void setRightArmPose(Rotations $$0) {
      this.entityData.set(DATA_RIGHT_ARM_POSE, $$0);
   }

   public void setLeftLegPose(Rotations $$0) {
      this.entityData.set(DATA_LEFT_LEG_POSE, $$0);
   }

   public void setRightLegPose(Rotations $$0) {
      this.entityData.set(DATA_RIGHT_LEG_POSE, $$0);
   }

   public Rotations getHeadPose() {
      return (Rotations)this.entityData.get(DATA_HEAD_POSE);
   }

   public Rotations getBodyPose() {
      return (Rotations)this.entityData.get(DATA_BODY_POSE);
   }

   public Rotations getLeftArmPose() {
      return (Rotations)this.entityData.get(DATA_LEFT_ARM_POSE);
   }

   public Rotations getRightArmPose() {
      return (Rotations)this.entityData.get(DATA_RIGHT_ARM_POSE);
   }

   public Rotations getLeftLegPose() {
      return (Rotations)this.entityData.get(DATA_LEFT_LEG_POSE);
   }

   public Rotations getRightLegPose() {
      return (Rotations)this.entityData.get(DATA_RIGHT_LEG_POSE);
   }

   @Override
   public boolean isPickable() {
      return super.isPickable() && !this.isMarker();
   }

   @Override
   public boolean skipAttackInteraction(net.minecraft.world.entity.Entity $$0) {
      return $$0 instanceof Player $$1 && !this.level().mayInteract($$1, this.blockPosition());
   }

   @Override
   public net.minecraft.world.entity.HumanoidArm getMainArm() {
      return net.minecraft.world.entity.HumanoidArm.RIGHT;
   }

   @Override
   public net.minecraft.world.entity.LivingEntity.Fallsounds getFallSounds() {
      return new net.minecraft.world.entity.LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.ARMOR_STAND_HIT;
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.ARMOR_STAND_BREAK;
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
   }

   @Override
   public boolean isAffectedByPotions() {
      return false;
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_CLIENT_FLAGS.equals($$0)) {
         this.refreshDimensions();
         this.blocksBuilding = !this.isMarker();
      }

      super.onSyncedDataUpdated($$0);
   }

   @Override
   public boolean attackable() {
      return false;
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.getDimensionsMarker(this.isMarker());
   }

   private net.minecraft.world.entity.EntityDimensions getDimensionsMarker(boolean $$0) {
      if ($$0) {
         return MARKER_DIMENSIONS;
      } else {
         return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
      }
   }

   @Override
   public Vec3 getLightProbePosition(float $$0) {
      if (this.isMarker()) {
         AABB $$1 = this.getDimensionsMarker(false).makeBoundingBox(this.position());
         BlockPos $$2 = this.blockPosition();
         int $$3 = Integer.MIN_VALUE;

         for (BlockPos $$4 : BlockPos.betweenClosed(BlockPos.containing($$1.minX, $$1.minY, $$1.minZ), BlockPos.containing($$1.maxX, $$1.maxY, $$1.maxZ))) {
            int $$5 = Math.max(this.level().getBrightness(LightLayer.BLOCK, $$4), this.level().getBrightness(LightLayer.SKY, $$4));
            if ($$5 == 15) {
               return Vec3.atCenterOf($$4);
            }

            if ($$5 > $$3) {
               $$3 = $$5;
               $$2 = $$4.immutable();
            }
         }

         return Vec3.atCenterOf($$2);
      } else {
         return super.getLightProbePosition($$0);
      }
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.ARMOR_STAND);
   }

   @Override
   public boolean canBeSeenByAnyone() {
      return !this.isInvisible() && !this.isMarker();
   }

   public void setArmorStandPose(ArmorStand.ArmorStandPose $$0) {
      this.setHeadPose($$0.head());
      this.setBodyPose($$0.body());
      this.setLeftArmPose($$0.leftArm());
      this.setRightArmPose($$0.rightArm());
      this.setLeftLegPose($$0.leftLeg());
      this.setRightLegPose($$0.rightLeg());
   }

   public ArmorStand.ArmorStandPose getArmorStandPose() {
      return new ArmorStand.ArmorStandPose(
         this.getHeadPose(), this.getBodyPose(), this.getLeftArmPose(), this.getRightArmPose(), this.getLeftLegPose(), this.getRightLegPose()
      );
   }

   public record ArmorStandPose(Rotations head, Rotations body, Rotations leftArm, Rotations rightArm, Rotations leftLeg, Rotations rightLeg) {
      public static final ArmorStand.ArmorStandPose DEFAULT = new ArmorStand.ArmorStandPose(
         ArmorStand.DEFAULT_HEAD_POSE,
         ArmorStand.DEFAULT_BODY_POSE,
         ArmorStand.DEFAULT_LEFT_ARM_POSE,
         ArmorStand.DEFAULT_RIGHT_ARM_POSE,
         ArmorStand.DEFAULT_LEFT_LEG_POSE,
         ArmorStand.DEFAULT_RIGHT_LEG_POSE
      );
      public static final Codec<ArmorStand.ArmorStandPose> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Rotations.CODEC.optionalFieldOf("Head", ArmorStand.DEFAULT_HEAD_POSE).forGetter(ArmorStand.ArmorStandPose::head),
               Rotations.CODEC.optionalFieldOf("Body", ArmorStand.DEFAULT_BODY_POSE).forGetter(ArmorStand.ArmorStandPose::body),
               Rotations.CODEC.optionalFieldOf("LeftArm", ArmorStand.DEFAULT_LEFT_ARM_POSE).forGetter(ArmorStand.ArmorStandPose::leftArm),
               Rotations.CODEC.optionalFieldOf("RightArm", ArmorStand.DEFAULT_RIGHT_ARM_POSE).forGetter(ArmorStand.ArmorStandPose::rightArm),
               Rotations.CODEC.optionalFieldOf("LeftLeg", ArmorStand.DEFAULT_LEFT_LEG_POSE).forGetter(ArmorStand.ArmorStandPose::leftLeg),
               Rotations.CODEC.optionalFieldOf("RightLeg", ArmorStand.DEFAULT_RIGHT_LEG_POSE).forGetter(ArmorStand.ArmorStandPose::rightLeg)
            )
            .apply($$0, ArmorStand.ArmorStandPose::new)
      );
   }
}
