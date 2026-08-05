/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FireworkRocketEntity
extends Projectile
implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int DEFAULT_LIFE = 0;
    private static final int DEFAULT_LIFE_TIME = 0;
    private static final boolean DEFAULT_SHOT_AT_ANGLE = false;
    private int life = 0;
    private int lifetime = 0;
    private @Nullable LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> $$0, Level $$1) {
        super((EntityType<? extends Projectile>)$$0, $$1);
    }

    public FireworkRocketEntity(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        super((EntityType<? extends Projectile>)EntityType.FIREWORK_ROCKET, $$0);
        this.life = 0;
        this.setPos($$1, $$2, $$3);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, $$4.copy());
        int $$5 = 1;
        Fireworks $$6 = $$4.get(DataComponents.FIREWORKS);
        if ($$6 != null) {
            $$5 += $$6.flightDuration();
        }
        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * $$5 + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level $$0, @Nullable Entity $$1, double $$2, double $$3, double $$4, ItemStack $$5) {
        this($$0, $$2, $$3, $$4, $$5);
        this.setOwner($$1);
    }

    public FireworkRocketEntity(Level $$0, ItemStack $$1, LivingEntity $$2) {
        this($$0, $$2, $$2.getX(), $$2.getY(), $$2.getZ(), $$1);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of($$2.getId()));
        this.attachedToEntity = $$2;
    }

    public FireworkRocketEntity(Level $$0, ItemStack $$1, double $$2, double $$3, double $$4, boolean $$5) {
        this($$0, $$2, $$3, $$4, $$1);
        this.entityData.set(DATA_SHOT_AT_ANGLE, $$5);
    }

    public FireworkRocketEntity(Level $$0, ItemStack $$1, Entity $$2, double $$3, double $$4, double $$5, boolean $$6) {
        this($$0, $$1, $$3, $$4, $$5, $$6);
        this.setOwner($$2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ID_FIREWORKS_ITEM, FireworkRocketEntity.getDefaultItem());
        $$0.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        $$0.define(DATA_SHOT_AT_ANGLE, false);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        return $$0 < 4096.0 && !this.isAttachedToEntity();
    }

    @Override
    public boolean shouldRender(double $$0, double $$1, double $$2) {
        return super.shouldRender($$0, $$1, $$2) && !this.isAttachedToEntity();
    }

    @Override
    public void tick() {
        Level level;
        HitResult $$9;
        super.tick();
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent($$0 -> {
                    Entity $$1 = this.level().getEntity($$0);
                    if ($$1 instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity)$$1;
                    }
                });
            }
            if (this.attachedToEntity != null) {
                Vec3 $$5;
                if (this.attachedToEntity.isFallFlying()) {
                    Vec3 $$02 = this.attachedToEntity.getLookAngle();
                    double $$1 = 1.5;
                    double $$2 = 0.1;
                    Vec3 $$3 = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity.setDeltaMovement($$3.add($$02.x * 0.1 + ($$02.x * 1.5 - $$3.x) * 0.5, $$02.y * 0.1 + ($$02.y * 1.5 - $$3.y) * 0.5, $$02.z * 0.1 + ($$02.z * 1.5 - $$3.z) * 0.5));
                    Vec3 $$4 = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
                } else {
                    $$5 = Vec3.ZERO;
                }
                this.setPos(this.attachedToEntity.getX() + $$5.x, this.attachedToEntity.getY() + $$5.y, this.attachedToEntity.getZ() + $$5.z);
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }
            HitResult $$6 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        } else {
            if (!this.isShotAtAngle()) {
                double $$7 = this.horizontalCollision ? 1.0 : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply($$7, 1.0, $$7).add(0.0, 0.04, 0.0));
            }
            Vec3 $$8 = this.getDeltaMovement();
            $$9 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            this.move(MoverType.SELF, $$8);
            this.applyEffectsFromBlocks();
            this.setDeltaMovement($$8);
        }
        if (!this.noPhysics && this.isAlive() && $$9.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf($$9);
            this.needsSync = true;
        }
        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
        }
        ++this.life;
        if (this.level().isClientSide() && this.life % 2 < 2) {
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getDeltaMovement().y * 0.5, this.random.nextGaussian() * 0.05);
        }
        if (this.life > this.lifetime && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$10 = (ServerLevel)level;
            this.explode($$10);
        }
    }

    private void explode(ServerLevel $$0) {
        $$0.broadcastEntityEvent(this, (byte)17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage($$0);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        super.onHitEntity($$0);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            this.explode($$1);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult $$0) {
        BlockPos $$1 = new BlockPos($$0.getBlockPos());
        this.level().getBlockState($$1).entityInside(this.level(), $$1, this, InsideBlockEffectApplier.NOOP, true);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            if (this.hasExplosion()) {
                this.explode($$2);
            }
        }
        super.onHitBlock($$0);
    }

    private boolean hasExplosion() {
        return !this.getExplosions().isEmpty();
    }

    private void dealExplosionDamage(ServerLevel $$0) {
        float $$1 = 0.0f;
        List<FireworkExplosion> $$2 = this.getExplosions();
        if (!$$2.isEmpty()) {
            $$1 = 5.0f + (float)($$2.size() * 2);
        }
        if ($$1 > 0.0f) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurtServer($$0, this.damageSources().fireworks(this, this.getOwner()), 5.0f + (float)($$2.size() * 2));
            }
            double $$3 = 5.0;
            Vec3 $$4 = this.position();
            List<LivingEntity> $$5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0));
            for (LivingEntity $$6 : $$5) {
                if ($$6 == this.attachedToEntity || this.distanceToSqr($$6) > 25.0) continue;
                boolean $$7 = false;
                for (int $$8 = 0; $$8 < 2; ++$$8) {
                    Vec3 $$9 = new Vec3($$6.getX(), $$6.getY(0.5 * (double)$$8), $$6.getZ());
                    BlockHitResult $$10 = this.level().clip(new ClipContext($$4, $$9, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (((HitResult)$$10).getType() != HitResult.Type.MISS) continue;
                    $$7 = true;
                    break;
                }
                if (!$$7) continue;
                float $$11 = $$1 * (float)Math.sqrt((5.0 - (double)this.distanceTo($$6)) / 5.0);
                $$6.hurtServer($$0, this.damageSources().fireworks(this, this.getOwner()), $$11);
            }
        }
    }

    private boolean isAttachedToEntity() {
        return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
    }

    public boolean isShotAtAngle() {
        return this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 17 && this.level().isClientSide()) {
            Vec3 $$1 = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), $$1.x, $$1.y, $$1.z, this.getExplosions());
        }
        super.handleEntityEvent($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("Life", this.life);
        $$0.putInt("LifeTime", this.lifetime);
        $$0.store("FireworksItem", ItemStack.CODEC, this.getItem());
        $$0.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.life = $$0.getIntOr("Life", 0);
        this.lifetime = $$0.getIntOr("LifeTime", 0);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, $$0.read("FireworksItem", ItemStack.CODEC).orElse(FireworkRocketEntity.getDefaultItem()));
        this.entityData.set(DATA_SHOT_AT_ANGLE, $$0.getBooleanOr("ShotAtAngle", false));
    }

    private List<FireworkExplosion> getExplosions() {
        ItemStack $$0 = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        Fireworks $$1 = $$0.get(DataComponents.FIREWORKS);
        return $$1 != null ? $$1.explosions() : List.of();
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(DATA_ID_FIREWORKS_ITEM);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity $$0, DamageSource $$1) {
        double $$2 = $$0.position().x - this.position().x;
        double $$3 = $$0.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)$$2, (double)$$3);
    }
}

