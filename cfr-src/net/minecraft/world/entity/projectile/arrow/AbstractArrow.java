/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile.arrow;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.SlotAccess;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractArrow
extends Projectile {
    private static final double ARROW_BASE_DAMAGE = 2.0;
    private static final int SHAKE_TIME = 7;
    private static final float WATER_INERTIA = 0.6f;
    private static final float INERTIA = 0.99f;
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
    private @Nullable BlockState lastState;
    protected int inGroundTime;
    public Pickup pickup = Pickup.DISALLOWED;
    public int shakeTime = 0;
    private int life = 0;
    private double baseDamage = 2.0;
    private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
    private @Nullable IntOpenHashSet piercingIgnoreEntityIds;
    private @Nullable List<Entity> piercedAndKilledEntities;
    private ItemStack pickupItemStack = this.getDefaultPickupItem();
    private @Nullable ItemStack firedFromWeapon = null;

    protected AbstractArrow(EntityType<? extends AbstractArrow> $$0, Level $$1) {
        super((EntityType<? extends Projectile>)$$0, $$1);
    }

    protected AbstractArrow(EntityType<? extends AbstractArrow> $$0, double $$1, double $$2, double $$3, Level $$4, ItemStack $$5, @Nullable ItemStack $$6) {
        this($$0, $$4);
        this.pickupItemStack = $$5.copy();
        this.applyComponentsFromItemStack($$5);
        Unit $$7 = $$5.remove(DataComponents.INTANGIBLE_PROJECTILE);
        if ($$7 != null) {
            this.pickup = Pickup.CREATIVE_ONLY;
        }
        this.setPos($$1, $$2, $$3);
        if ($$6 != null && $$4 instanceof ServerLevel) {
            ServerLevel $$8 = (ServerLevel)$$4;
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

    protected AbstractArrow(EntityType<? extends AbstractArrow> $$0, LivingEntity $$1, Level $$2, ItemStack $$3, @Nullable ItemStack $$4) {
        this($$0, $$1.getX(), $$1.getEyeY() - (double)0.1f, $$1.getZ(), $$2, $$3, $$4);
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
        return $$0 < ($$1 *= 64.0 * AbstractArrow.getViewScale()) * $$1;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
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
        float $$10;
        VoxelShape $$4;
        boolean $$0 = !this.isNoPhysics();
        Vec3 $$1 = this.getDeltaMovement();
        BlockPos $$2 = this.blockPosition();
        BlockState $$3 = this.level().getBlockState($$2);
        if (!$$3.isAir() && $$0 && !($$4 = $$3.getCollisionShape(this.level(), $$2)).isEmpty()) {
            Vec3 $$5 = this.position();
            for (AABB $$6 : $$4.toAabbs()) {
                if (!$$6.move($$2).contains($$5)) continue;
                this.setDeltaMovement(Vec3.ZERO);
                this.setInGround(true);
                break;
            }
        }
        if (this.shakeTime > 0) {
            --this.shakeTime;
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
            ++this.inGroundTime;
            if (this.isAlive()) {
                this.applyEffectsFromBlocks();
            }
            if (!this.level().isClientSide()) {
                this.setSharedFlagOnFire(this.getRemainingFireTicks() > 0);
            }
            return;
        }
        this.inGroundTime = 0;
        Vec3 $$7 = this.position();
        if (this.isInWater()) {
            this.applyInertia(this.getWaterInertia());
            this.addBubbleParticles($$7);
        }
        if (this.isCritArrow()) {
            for (int $$8 = 0; $$8 < 4; ++$$8) {
                this.level().addParticle(ParticleTypes.CRIT, $$7.x + $$1.x * (double)$$8 / 4.0, $$7.y + $$1.y * (double)$$8 / 4.0, $$7.z + $$1.z * (double)$$8 / 4.0, -$$1.x, -$$1.y + 0.2, -$$1.z);
            }
        }
        if (!$$0) {
            float $$9 = (float)(Mth.atan2(-$$1.x, -$$1.z) * 57.2957763671875);
        } else {
            $$10 = (float)(Mth.atan2($$1.x, $$1.z) * 57.2957763671875);
        }
        float $$11 = (float)(Mth.atan2($$1.y, $$1.horizontalDistance()) * 57.2957763671875);
        this.setXRot(AbstractArrow.lerpRotation(this.getXRot(), $$11));
        this.setYRot(AbstractArrow.lerpRotation(this.getYRot(), $$10));
        this.checkLeftOwner();
        if ($$0) {
            BlockHitResult $$12 = this.level().clipIncludingBorder(new ClipContext($$7, $$7.add($$1), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            this.stepMoveAndHit($$12);
        } else {
            this.setPos($$7.add($$1));
            this.applyEffectsFromBlocks();
        }
        if (!this.isInWater()) {
            this.applyInertia(0.99f);
        }
        if ($$0 && !this.isInGround()) {
            this.applyGravity();
        }
        super.tick();
    }

    private void stepMoveAndHit(BlockHitResult $$0) {
        while (this.isAlive()) {
            Vec3 $$12 = this.position();
            ArrayList<EntityHitResult> $$2 = new ArrayList<EntityHitResult>(this.findHitEntities($$12, $$0.getLocation()));
            $$2.sort(Comparator.comparingDouble($$1 -> $$12.distanceToSqr($$1.getEntity().position())));
            EntityHitResult $$3 = $$2.isEmpty() ? null : $$2.getFirst();
            Vec3 $$4 = ((HitResult)Objects.requireNonNullElse($$3, $$0)).getLocation();
            this.setPos($$4);
            this.applyEffectsFromBlocks($$12, $$4);
            if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
                this.handlePortal();
            }
            if ($$2.isEmpty()) {
                if (!this.isAlive() || $$0.getType() == HitResult.Type.MISS) break;
                this.hitTargetOrDeflectSelf($$0);
                this.needsSync = true;
                break;
            }
            if (!this.isAlive() || this.noPhysics) continue;
            ProjectileDeflection $$5 = this.hitTargetsOrDeflectSelf($$2);
            this.needsSync = true;
            if (this.getPierceLevel() > 0 && $$5 == ProjectileDeflection.NONE) continue;
            break;
        }
    }

    private ProjectileDeflection hitTargetsOrDeflectSelf(Collection<EntityHitResult> $$0) {
        for (EntityHitResult $$1 : $$0) {
            ProjectileDeflection $$2 = this.hitTargetOrDeflectSelf($$1);
            if (this.isAlive() && $$2 == ProjectileDeflection.NONE) continue;
            return $$2;
        }
        return ProjectileDeflection.NONE;
    }

    private void applyInertia(float $$0) {
        Vec3 $$1 = this.getDeltaMovement();
        this.setDeltaMovement($$1.scale($$0));
    }

    private void addBubbleParticles(Vec3 $$0) {
        Vec3 $$1 = this.getDeltaMovement();
        for (int $$2 = 0; $$2 < 4; ++$$2) {
            float $$3 = 0.25f;
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
        this.setDeltaMovement($$0.multiply(this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f, this.random.nextFloat() * 0.2f));
        this.life = 0;
    }

    protected boolean isInGround() {
        return this.entityData.get(IN_GROUND);
    }

    protected void setInGround(boolean $$0) {
        this.entityData.set(IN_GROUND, $$0);
    }

    @Override
    public boolean isPushedByFluid() {
        return !this.isInGround();
    }

    @Override
    public void move(MoverType $$0, Vec3 $$1) {
        super.move($$0, $$1);
        if ($$0 != MoverType.SELF && this.shouldFall()) {
            this.startFalling();
        }
    }

    protected void tickDespawn() {
        ++this.life;
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
        if (this.isInGround()) {
            return;
        }
        super.onAboveBubbleColumn($$0, $$1);
    }

    @Override
    public void onInsideBubbleColumn(boolean $$0) {
        if (this.isInGround()) {
            return;
        }
        super.onInsideBubbleColumn($$0);
    }

    @Override
    public void push(double $$0, double $$1, double $$2) {
        if (this.isInGround()) {
            return;
        }
        super.push($$0, $$1, $$2);
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        Level level;
        super.onHitEntity($$0);
        Entity $$1 = $$0.getEntity();
        float $$2 = (float)this.getDeltaMovement().length();
        double $$3 = this.baseDamage;
        Entity $$4 = this.getOwner();
        DamageSource $$5 = this.damageSources().arrow(this, $$4 != null ? $$4 : this);
        if (this.getWeaponItem() != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$6 = (ServerLevel)level;
            $$3 = EnchantmentHelper.modifyDamage($$6, this.getWeaponItem(), $$1, $$5, (float)$$3);
        }
        int $$7 = Mth.ceil(Mth.clamp((double)$$2 * $$3, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }
            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity((int)5);
            }
            if (this.piercingIgnoreEntityIds.size() < this.getPierceLevel() + 1) {
                this.piercingIgnoreEntityIds.add($$1.getId());
            } else {
                this.discard();
                return;
            }
        }
        if (this.isCritArrow()) {
            long $$8 = this.random.nextInt($$7 / 2 + 2);
            $$7 = (int)Math.min($$8 + (long)$$7, Integer.MAX_VALUE);
        }
        if ($$4 instanceof LivingEntity) {
            LivingEntity $$9 = (LivingEntity)$$4;
            $$9.setLastHurtMob($$1);
        }
        boolean $$10 = $$1.getType() == EntityType.ENDERMAN;
        int $$11 = $$1.getRemainingFireTicks();
        if (this.isOnFire() && !$$10) {
            $$1.igniteForSeconds(5.0f);
        }
        if ($$1.hurtOrSimulate($$5, $$7)) {
            if ($$10) {
                return;
            }
            if ($$1 instanceof LivingEntity) {
                LivingEntity $$12 = (LivingEntity)$$1;
                if (!this.level().isClientSide() && this.getPierceLevel() <= 0) {
                    $$12.setArrowCount($$12.getArrowCount() + 1);
                }
                this.doKnockback($$12, $$5);
                Level level2 = this.level();
                if (level2 instanceof ServerLevel) {
                    ServerLevel $$13 = (ServerLevel)level2;
                    EnchantmentHelper.doPostAttackEffectsWithItemSource($$13, $$12, $$5, this.getWeaponItem());
                }
                this.doPostHurtEffects($$12);
                if ($$12 instanceof Player && $$4 instanceof ServerPlayer) {
                    ServerPlayer $$14 = (ServerPlayer)$$4;
                    if (!this.isSilent() && $$12 != $$14) {
                        $$14.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND, 0.0f));
                    }
                }
                if (!$$1.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add($$12);
                }
                if (!this.level().isClientSide() && $$4 instanceof ServerPlayer) {
                    ServerPlayer $$15 = (ServerPlayer)$$4;
                    if (this.piercedAndKilledEntities != null) {
                        CriteriaTriggers.KILLED_BY_ARROW.trigger($$15, this.piercedAndKilledEntities, this.firedFromWeapon);
                    } else if (!$$1.isAlive()) {
                        CriteriaTriggers.KILLED_BY_ARROW.trigger($$15, List.of($$1), this.firedFromWeapon);
                    }
                }
            }
            this.playSound(this.soundEvent, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            $$1.setRemainingFireTicks($$11);
            this.deflect(ProjectileDeflection.REVERSE, $$1, this.owner, false);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.2));
            Level level3 = this.level();
            if (level3 instanceof ServerLevel) {
                ServerLevel $$16 = (ServerLevel)level3;
                if (this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                    if (this.pickup == Pickup.ALLOWED) {
                        this.spawnAtLocation($$16, this.getPickupItem(), 0.1f);
                    }
                    this.discard();
                }
            }
        }
    }

    protected void doKnockback(LivingEntity $$0, DamageSource $$1) {
        float f;
        Level level;
        if (this.firedFromWeapon != null && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            f = EnchantmentHelper.modifyKnockback($$2, this.firedFromWeapon, $$0, $$1, 0.0f);
        } else {
            f = 0.0f;
        }
        double $$3 = f;
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
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            if ($$1 != null) {
                this.hitBlockEnchantmentEffects($$2, $$0, $$1);
            }
        }
        Vec3 $$3 = this.getDeltaMovement();
        Vec3 $$4 = new Vec3(Math.signum($$3.x), Math.signum($$3.y), Math.signum($$3.z));
        Vec3 $$5 = $$4.scale(0.05f);
        this.setPos(this.position().subtract($$5));
        this.setDeltaMovement(Vec3.ZERO);
        this.playSound(this.getHitGroundSoundEvent(), 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
        this.setInGround(true);
        this.shakeTime = 7;
        this.setCritArrow(false);
        this.setPierceLevel((byte)0);
        this.setSoundEvent(SoundEvents.ARROW_HIT);
        this.resetPiercedEntities();
    }

    protected void hitBlockEnchantmentEffects(ServerLevel $$02, BlockHitResult $$1, ItemStack $$2) {
        LivingEntity $$4;
        Vec3 $$3 = $$1.getBlockPos().clampLocationWithin($$1.getLocation());
        Entity entity = this.getOwner();
        EnchantmentHelper.onHitBlock($$02, $$2, entity instanceof LivingEntity ? ($$4 = (LivingEntity)entity) : null, this, null, $$3, $$02.getBlockState($$1.getBlockPos()), $$0 -> {
            this.firedFromWeapon = null;
        });
    }

    @Override
    public @Nullable ItemStack getWeaponItem() {
        return this.firedFromWeapon;
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    protected final SoundEvent getHitGroundSoundEvent() {
        return this.soundEvent;
    }

    protected void doPostHurtEffects(LivingEntity $$0) {
    }

    protected @Nullable EntityHitResult findHitEntity(Vec3 $$0, Vec3 $$1) {
        return ProjectileUtil.getEntityHitResult(this.level(), this, $$0, $$1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity);
    }

    protected Collection<EntityHitResult> findHitEntities(Vec3 $$0, Vec3 $$1) {
        return ProjectileUtil.getManyEntityHitResult(this.level(), this, $$0, $$1, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), this::canHitEntity, false);
    }

    @Override
    protected boolean canHitEntity(Entity $$0) {
        Player $$1;
        Entity entity;
        if ($$0 instanceof Player && (entity = this.getOwner()) instanceof Player && !($$1 = (Player)entity).canHarmPlayer((Player)$$0)) {
            return false;
        }
        return super.canHitEntity($$0) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains($$0.getId()));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putShort("life", (short)this.life);
        $$0.storeNullable("inBlockState", BlockState.CODEC, this.lastState);
        $$0.putByte("shake", (byte)this.shakeTime);
        $$0.putBoolean("inGround", this.isInGround());
        $$0.store("pickup", Pickup.LEGACY_CODEC, this.pickup);
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
        this.lastState = $$0.read("inBlockState", BlockState.CODEC).orElse(null);
        this.shakeTime = $$0.getByteOr("shake", (byte)0) & 0xFF;
        this.setInGround($$0.getBooleanOr("inGround", false));
        this.baseDamage = $$0.getDoubleOr("damage", 2.0);
        this.pickup = $$0.read("pickup", Pickup.LEGACY_CODEC).orElse(Pickup.DISALLOWED);
        this.setCritArrow($$0.getBooleanOr("crit", false));
        this.setPierceLevel($$0.getByteOr("PierceLevel", (byte)0));
        this.soundEvent = $$0.read("SoundEvent", BuiltInRegistries.SOUND_EVENT.byNameCodec()).orElse(this.getDefaultHitGroundSoundEvent());
        this.setPickupItemStack($$0.read("item", ItemStack.CODEC).orElse(this.getDefaultPickupItem()));
        this.firedFromWeapon = $$0.read("weapon", ItemStack.CODEC).orElse(null);
    }

    @Override
    public void setOwner(@Nullable Entity $$0) {
        Pickup pickup;
        super.setOwner($$0);
        Entity entity = $$0;
        int n = 0;
        block4: while (true) {
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Player.class, OminousItemSpawner.class}, (Object)entity, n)) {
                case 0: {
                    Player $$1 = (Player)entity;
                    if (this.pickup != Pickup.DISALLOWED) {
                        n = 1;
                        continue block4;
                    }
                    pickup = Pickup.ALLOWED;
                    break block4;
                }
                case 1: {
                    OminousItemSpawner $$2 = (OminousItemSpawner)entity;
                    pickup = Pickup.DISALLOWED;
                    break block4;
                }
                default: {
                    pickup = this.pickup;
                    break block4;
                }
            }
            break;
        }
        this.pickup = pickup;
    }

    @Override
    public void playerTouch(Player $$0) {
        if (this.level().isClientSide() || !this.isInGround() && !this.isNoPhysics() || this.shakeTime > 0) {
            return;
        }
        if (this.tryPickup($$0)) {
            $$0.take(this, 1);
            this.discard();
        }
    }

    protected boolean tryPickup(Player $$0) {
        return switch (this.pickup.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 1 -> $$0.getInventory().add(this.getPickupItem());
            case 2 -> $$0.hasInfiniteMaterials();
        };
    }

    protected ItemStack getPickupItem() {
        return this.pickupItemStack.copy();
    }

    protected abstract ItemStack getDefaultPickupItem();

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
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
        byte $$2 = this.entityData.get(ID_FLAGS);
        if ($$1) {
            this.entityData.set(ID_FLAGS, (byte)($$2 | $$0));
        } else {
            this.entityData.set(ID_FLAGS, (byte)($$2 & ~$$0));
        }
    }

    protected void setPickupItemStack(ItemStack $$0) {
        this.pickupItemStack = !$$0.isEmpty() ? $$0 : this.getDefaultPickupItem();
    }

    public boolean isCritArrow() {
        byte $$0 = this.entityData.get(ID_FLAGS);
        return ($$0 & 1) != 0;
    }

    public byte getPierceLevel() {
        return this.entityData.get(PIERCE_LEVEL);
    }

    public void setBaseDamageFromMob(float $$0) {
        this.setBaseDamage((double)($$0 * 2.0f) + this.random.triangle((double)this.level().getDifficulty().getId() * 0.11, 0.57425));
    }

    protected float getWaterInertia() {
        return 0.6f;
    }

    public void setNoPhysics(boolean $$0) {
        this.noPhysics = $$0;
        this.setFlag(2, $$0);
    }

    public boolean isNoPhysics() {
        if (!this.level().isClientSide()) {
            return this.noPhysics;
        }
        return (this.entityData.get(ID_FLAGS) & 2) != 0;
    }

    @Override
    public boolean isPickable() {
        return super.isPickable() && !this.isInGround();
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        if ($$0 == 0) {
            return SlotAccess.of(this::getPickupItemStackOrigin, this::setPickupItemStack);
        }
        return super.getSlot($$0);
    }

    @Override
    protected boolean shouldBounceOnWorldBorder() {
        return true;
    }

    public static enum Pickup {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY;

        public static final Codec<Pickup> LEGACY_CODEC;

        public static Pickup byOrdinal(int $$0) {
            if ($$0 < 0 || $$0 > Pickup.values().length) {
                $$0 = 0;
            }
            return Pickup.values()[$$0];
        }

        static {
            LEGACY_CODEC = Codec.BYTE.xmap(Pickup::byOrdinal, $$0 -> (byte)$$0.ordinal());
        }
    }
}

