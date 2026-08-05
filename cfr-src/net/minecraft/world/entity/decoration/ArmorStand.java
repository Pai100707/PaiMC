/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.decoration;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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

public class ArmorStand
extends LivingEntity {
    public static final int WOBBLE_TIME = 5;
    private static final boolean ENABLE_ARMS = true;
    public static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    public static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0f, 0.0f, 0.0f);
    public static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0f, 0.0f, -10.0f);
    public static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0f, 0.0f, 10.0f);
    public static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0f, 0.0f, -1.0f);
    public static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0f, 0.0f, 1.0f);
    private static final EntityDimensions MARKER_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5f).withEyeHeight(0.9875f);
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
    private static final Predicate<Entity> RIDABLE_MINECARTS = $$0 -> {
        AbstractMinecart $$1;
        return $$0 instanceof AbstractMinecart && ($$1 = (AbstractMinecart)$$0).isRideable();
    };
    private static final boolean DEFAULT_INVISIBLE = false;
    private static final int DEFAULT_DISABLED_SLOTS = 0;
    private static final boolean DEFAULT_SMALL = false;
    private static final boolean DEFAULT_SHOW_ARMS = false;
    private static final boolean DEFAULT_NO_BASE_PLATE = false;
    private static final boolean DEFAULT_MARKER = false;
    private boolean invisible = false;
    public long lastHit;
    private int disabledSlots = 0;

    public ArmorStand(EntityType<? extends ArmorStand> $$0, Level $$1) {
        super((EntityType<? extends LivingEntity>)$$0, $$1);
    }

    public ArmorStand(Level $$0, double $$1, double $$2, double $$3) {
        this((EntityType<? extends ArmorStand>)EntityType.ARMOR_STAND, $$0);
        this.setPos($$1, $$2, $$3);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ArmorStand.createLivingAttributes().add(Attributes.STEP_HEIGHT, 0.0);
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
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
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
    public boolean canUseSlot(EquipmentSlot $$0) {
        return $$0 != EquipmentSlot.BODY && $$0 != EquipmentSlot.SADDLE && !this.isDisabled($$0);
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
        $$0.store("Pose", ArmorStandPose.CODEC, this.getArmorStandPose());
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
        $$0.read("Pose", ArmorStandPose.CODEC).ifPresent(this::setArmorStandPose);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity $$0) {
    }

    @Override
    protected void pushEntities() {
        List<Entity> $$0 = this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);
        for (Entity $$1 : $$0) {
            if (!(this.distanceToSqr($$1) <= 0.2)) continue;
            $$1.push(this);
        }
    }

    @Override
    public InteractionResult interactAt(Player $$0, Vec3 $$1, InteractionHand $$2) {
        ItemStack $$3 = $$0.getItemInHand($$2);
        if (this.isMarker() || $$3.is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if ($$0.isSpectator()) {
            return InteractionResult.SUCCESS;
        }
        if ($$0.level().isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }
        EquipmentSlot $$4 = this.getEquipmentSlotForItem($$3);
        if ($$3.isEmpty()) {
            EquipmentSlot $$6;
            EquipmentSlot $$5 = this.getClickedSlot($$1);
            EquipmentSlot equipmentSlot = $$6 = this.isDisabled($$5) ? $$4 : $$5;
            if (this.hasItemInSlot($$6) && this.swapItem($$0, $$6, $$3, $$2)) {
                return InteractionResult.SUCCESS_SERVER;
            }
        } else {
            if (this.isDisabled($$4)) {
                return InteractionResult.FAIL;
            }
            if ($$4.getType() == EquipmentSlot.Type.HAND && !this.showArms()) {
                return InteractionResult.FAIL;
            }
            if (this.swapItem($$0, $$4, $$3, $$2)) {
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private EquipmentSlot getClickedSlot(Vec3 $$0) {
        EquipmentSlot $$1 = EquipmentSlot.MAINHAND;
        boolean $$2 = this.isSmall();
        double $$3 = $$0.y / (double)(this.getScale() * this.getAgeScale());
        EquipmentSlot $$4 = EquipmentSlot.FEET;
        if ($$3 >= 0.1) {
            double d = $$2 ? 0.8 : 0.45;
            if ($$3 < 0.1 + d && this.hasItemInSlot($$4)) {
                return EquipmentSlot.FEET;
            }
        }
        double d = $$2 ? 0.3 : 0.0;
        if ($$3 >= 0.9 + d) {
            double d2 = $$2 ? 1.0 : 0.7;
            if ($$3 < 0.9 + d2 && this.hasItemInSlot(EquipmentSlot.CHEST)) {
                return EquipmentSlot.CHEST;
            }
        }
        if ($$3 >= 0.4) {
            double d3 = $$2 ? 1.0 : 0.8;
            if ($$3 < 0.4 + d3 && this.hasItemInSlot(EquipmentSlot.LEGS)) {
                return EquipmentSlot.LEGS;
            }
        }
        if ($$3 >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        }
        if (this.hasItemInSlot(EquipmentSlot.MAINHAND)) return $$1;
        if (!this.hasItemInSlot(EquipmentSlot.OFFHAND)) return $$1;
        return EquipmentSlot.OFFHAND;
    }

    private boolean isDisabled(EquipmentSlot $$0) {
        return (this.disabledSlots & 1 << $$0.getFilterBit(0)) != 0 || $$0.getType() == EquipmentSlot.Type.HAND && !this.showArms();
    }

    private boolean swapItem(Player $$0, EquipmentSlot $$1, ItemStack $$2, InteractionHand $$3) {
        ItemStack $$4 = this.getItemBySlot($$1);
        if (!$$4.isEmpty() && (this.disabledSlots & 1 << $$1.getFilterBit(8)) != 0) {
            return false;
        }
        if ($$4.isEmpty() && (this.disabledSlots & 1 << $$1.getFilterBit(16)) != 0) {
            return false;
        }
        if ($$0.hasInfiniteMaterials() && $$4.isEmpty() && !$$2.isEmpty()) {
            this.setItemSlot($$1, $$2.copyWithCount(1));
            return true;
        }
        if (!$$2.isEmpty() && $$2.getCount() > 1) {
            if (!$$4.isEmpty()) {
                return false;
            }
            this.setItemSlot($$1, $$2.split(1));
            return true;
        }
        this.setItemSlot($$1, $$2);
        $$0.setItemInHand($$3, $$4);
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isRemoved()) {
            return false;
        }
        if (!$$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue() && $$1.getEntity() instanceof Mob) {
            return false;
        }
        if ($$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill($$0);
            return false;
        }
        if (this.isInvulnerableTo($$0, $$1) || this.invisible || this.isMarker()) {
            return false;
        }
        if ($$1.is(DamageTypeTags.IS_EXPLOSION)) {
            this.brokenByAnything($$0, $$1);
            this.kill($$0);
            return false;
        }
        if ($$1.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.causeDamage($$0, $$1, 0.15f);
            } else {
                this.igniteForSeconds(5.0f);
            }
            return false;
        }
        if ($$1.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5f) {
            this.causeDamage($$0, $$1, 4.0f);
            return false;
        }
        boolean $$3 = $$1.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
        boolean $$4 = $$1.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
        if (!$$3 && !$$4) {
            return false;
        }
        Entity entity = $$1.getEntity();
        if (entity instanceof Player) {
            Player $$5 = (Player)entity;
            if (!$$5.getAbilities().mayBuild) {
                return false;
            }
        }
        if ($$1.isCreativePlayer()) {
            this.playBrokenSound();
            this.showBreakingParticles();
            this.kill($$0);
            return true;
        }
        long $$6 = $$0.getGameTime();
        if ($$6 - this.lastHit <= 5L || $$4) {
            this.brokenByPlayer($$0, $$1);
            this.showBreakingParticles();
            this.kill($$0);
        } else {
            $$0.broadcastEntityEvent(this, (byte)32);
            this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
            this.lastHit = $$6;
        }
        return true;
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 32) {
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3f, 1.0f, false);
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
        return $$0 < ($$1 *= 64.0) * $$1;
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666), this.getZ(), 10, this.getBbWidth() / 4.0f, this.getBbHeight() / 4.0f, this.getBbWidth() / 4.0f, 0.05);
        }
    }

    private void causeDamage(ServerLevel $$0, DamageSource $$1, float $$2) {
        float $$3 = this.getHealth();
        if (($$3 -= $$2) <= 0.5f) {
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
        for (EquipmentSlot $$2 : EquipmentSlot.VALUES) {
            ItemStack $$3 = this.equipment.set($$2, ItemStack.EMPTY);
            if ($$3.isEmpty()) continue;
            Block.popResource(this.level(), this.blockPosition().above(), $$3);
        }
    }

    private void playBrokenSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0f, 1.0f);
    }

    @Override
    protected void tickHeadTurn(float $$0) {
        this.yBodyRotO = this.yRotO;
        this.yBodyRot = this.getYRot();
    }

    @Override
    public void travel(Vec3 $$0) {
        if (!this.hasPhysics()) {
            return;
        }
        super.travel($$0);
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
        this.remove(Entity.RemovalReason.KILLED);
        this.gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean ignoreExplosion(Explosion $$0) {
        if ($$0.shouldAffectBlocklikeEntities()) {
            return this.isInvisible();
        }
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        if (this.isMarker()) {
            return PushReaction.IGNORE;
        }
        return super.getPistonPushReaction();
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return this.isMarker();
    }

    private void setSmall(boolean $$0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 1, $$0));
    }

    public boolean isSmall() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
    }

    public void setShowArms(boolean $$0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, $$0));
    }

    public boolean showArms() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
    }

    public void setNoBasePlate(boolean $$0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 8, $$0));
    }

    public boolean showBasePlate() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 8) == 0;
    }

    private void setMarker(boolean $$0) {
        this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 16, $$0));
    }

    public boolean isMarker() {
        return (this.entityData.get(DATA_CLIENT_FLAGS) & 0x10) != 0;
    }

    private byte setBit(byte $$0, int $$1, boolean $$2) {
        $$0 = $$2 ? (byte)($$0 | $$1) : (byte)($$0 & ~$$1);
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
        return this.entityData.get(DATA_HEAD_POSE);
    }

    public Rotations getBodyPose() {
        return this.entityData.get(DATA_BODY_POSE);
    }

    public Rotations getLeftArmPose() {
        return this.entityData.get(DATA_LEFT_ARM_POSE);
    }

    public Rotations getRightArmPose() {
        return this.entityData.get(DATA_RIGHT_ARM_POSE);
    }

    public Rotations getLeftLegPose() {
        return this.entityData.get(DATA_LEFT_LEG_POSE);
    }

    public Rotations getRightLegPose() {
        return this.entityData.get(DATA_RIGHT_LEG_POSE);
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isMarker();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean skipAttackInteraction(Entity $$0) {
        if (!($$0 instanceof Player)) return false;
        Player $$1 = (Player)$$0;
        if (this.level().mayInteract($$1, this.blockPosition())) return false;
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.ARMOR_STAND_FALL, SoundEvents.ARMOR_STAND_FALL);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.ARMOR_STAND_HIT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public void thunderHit(ServerLevel $$0, LightningBolt $$1) {
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
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.getDimensionsMarker(this.isMarker());
    }

    private EntityDimensions getDimensionsMarker(boolean $$0) {
        if ($$0) {
            return MARKER_DIMENSIONS;
        }
        return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
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
                if ($$5 <= $$3) continue;
                $$3 = $$5;
                $$2 = $$4.immutable();
            }
            return Vec3.atCenterOf($$2);
        }
        return super.getLightProbePosition($$0);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.ARMOR_STAND);
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return !this.isInvisible() && !this.isMarker();
    }

    public void setArmorStandPose(ArmorStandPose $$0) {
        this.setHeadPose($$0.head());
        this.setBodyPose($$0.body());
        this.setLeftArmPose($$0.leftArm());
        this.setRightArmPose($$0.rightArm());
        this.setLeftLegPose($$0.leftLeg());
        this.setRightLegPose($$0.rightLeg());
    }

    public ArmorStandPose getArmorStandPose() {
        return new ArmorStandPose(this.getHeadPose(), this.getBodyPose(), this.getLeftArmPose(), this.getRightArmPose(), this.getLeftLegPose(), this.getRightLegPose());
    }

    public record ArmorStandPose(Rotations head, Rotations body, Rotations leftArm, Rotations rightArm, Rotations leftLeg, Rotations rightLeg) {
        public static final ArmorStandPose DEFAULT = new ArmorStandPose(DEFAULT_HEAD_POSE, DEFAULT_BODY_POSE, DEFAULT_LEFT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE, DEFAULT_LEFT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
        public static final Codec<ArmorStandPose> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Rotations.CODEC.optionalFieldOf("Head", (Object)DEFAULT_HEAD_POSE).forGetter(ArmorStandPose::head), (App)Rotations.CODEC.optionalFieldOf("Body", (Object)DEFAULT_BODY_POSE).forGetter(ArmorStandPose::body), (App)Rotations.CODEC.optionalFieldOf("LeftArm", (Object)DEFAULT_LEFT_ARM_POSE).forGetter(ArmorStandPose::leftArm), (App)Rotations.CODEC.optionalFieldOf("RightArm", (Object)DEFAULT_RIGHT_ARM_POSE).forGetter(ArmorStandPose::rightArm), (App)Rotations.CODEC.optionalFieldOf("LeftLeg", (Object)DEFAULT_LEFT_LEG_POSE).forGetter(ArmorStandPose::leftLeg), (App)Rotations.CODEC.optionalFieldOf("RightLeg", (Object)DEFAULT_RIGHT_LEG_POSE).forGetter(ArmorStandPose::rightLeg)).apply((Applicative)$$0, ArmorStandPose::new));
    }
}

