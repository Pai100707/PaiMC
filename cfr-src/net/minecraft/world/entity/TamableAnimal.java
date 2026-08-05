/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public abstract class TamableAnimal
extends Animal
implements OwnableEntity {
    public static final int TELEPORT_WHEN_DISTANCE_IS_SQ = 144;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_TARGET_AFTER_TELEPORTING = 1;
    private static final boolean DEFAULT_ORDERED_TO_SIT = false;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableAnimal.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private boolean orderedToSit = false;

    protected TamableAnimal(EntityType<? extends TamableAnimal> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_FLAGS_ID, (byte)0);
        $$0.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        EntityReference<LivingEntity> $$1 = this.getOwnerReference();
        EntityReference.store($$1, $$0, "Owner");
        $$0.putBoolean("Sitting", this.orderedToSit);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        EntityReference $$1 = EntityReference.readWithOldOwnerConversion($$0, "Owner", this.level());
        if ($$1 != null) {
            try {
                this.entityData.set(DATA_OWNERUUID_ID, Optional.of($$1));
                this.setTame(true, false);
            }
            catch (Throwable $$2) {
                this.setTame(false, true);
            }
        } else {
            this.entityData.set(DATA_OWNERUUID_ID, Optional.empty());
            this.setTame(false, true);
        }
        this.orderedToSit = $$0.getBooleanOr("Sitting", false);
        this.setInSittingPose(this.orderedToSit);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    protected void spawnTamingParticles(boolean $$0) {
        SimpleParticleType $$1 = ParticleTypes.HEART;
        if (!$$0) {
            $$1 = ParticleTypes.SMOKE;
        }
        for (int $$2 = 0; $$2 < 7; ++$$2) {
            double $$3 = this.random.nextGaussian() * 0.02;
            double $$4 = this.random.nextGaussian() * 0.02;
            double $$5 = this.random.nextGaussian() * 0.02;
            this.level().addParticle($$1, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), $$3, $$4, $$5);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 7) {
            this.spawnTamingParticles(true);
        } else if ($$0 == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void setTame(boolean $$0, boolean $$1) {
        byte $$2 = this.entityData.get(DATA_FLAGS_ID);
        if ($$0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$2 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$2 & 0xFFFFFFFB));
        }
        if ($$1) {
            this.applyTamingSideEffects();
        }
    }

    protected void applyTamingSideEffects() {
    }

    public boolean isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setInSittingPose(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_FLAGS_ID);
        if ($$0) {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$1 | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)($$1 & 0xFFFFFFFE));
        }
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity $$0) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable($$0).map(EntityReference::of));
    }

    public void setOwnerReference(@Nullable EntityReference<LivingEntity> $$0) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable($$0));
    }

    public void tame(Player $$0) {
        this.setTame(true, true);
        this.setOwner($$0);
        if ($$0 instanceof ServerPlayer) {
            ServerPlayer $$1 = (ServerPlayer)$$0;
            CriteriaTriggers.TAME_ANIMAL.trigger($$1, this);
        }
    }

    @Override
    public boolean canAttack(LivingEntity $$0) {
        if (this.isOwnedBy($$0)) {
            return false;
        }
        return super.canAttack($$0);
    }

    public boolean isOwnedBy(LivingEntity $$0) {
        return $$0 == this.getOwner();
    }

    public boolean wantsToAttack(LivingEntity $$0, LivingEntity $$1) {
        return true;
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity $$1;
        PlayerTeam $$0 = super.getTeam();
        if ($$0 != null) {
            return $$0;
        }
        if (this.isTame() && ($$1 = this.getRootOwner()) != null) {
            return $$1.getTeam();
        }
        return null;
    }

    @Override
    protected boolean considersEntityAsAlly(Entity $$0) {
        if (this.isTame()) {
            LivingEntity $$1 = this.getRootOwner();
            if ($$0 == $$1) {
                return true;
            }
            if ($$1 != null) {
                return $$1.considersEntityAsAlly($$0);
            }
        }
        return super.considersEntityAsAlly($$0);
    }

    @Override
    public void die(DamageSource $$0) {
        LivingEntity livingEntity;
        ServerLevel $$1;
        Level level = this.level();
        if (level instanceof ServerLevel && ($$1 = (ServerLevel)level).getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).booleanValue() && (livingEntity = this.getOwner()) instanceof ServerPlayer) {
            ServerPlayer $$2 = (ServerPlayer)livingEntity;
            $$2.sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }
        super.die($$0);
    }

    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    public void setOrderedToSit(boolean $$0) {
        this.orderedToSit = $$0;
    }

    public void tryToTeleportToOwner() {
        LivingEntity $$0 = this.getOwner();
        if ($$0 != null) {
            this.teleportToAroundBlockPos($$0.blockPosition());
        }
    }

    public boolean shouldTryTeleportToOwner() {
        LivingEntity $$0 = this.getOwner();
        return $$0 != null && this.distanceToSqr(this.getOwner()) >= 144.0;
    }

    private void teleportToAroundBlockPos(BlockPos $$0) {
        for (int $$1 = 0; $$1 < 10; ++$$1) {
            int $$2 = this.random.nextIntBetweenInclusive(-3, 3);
            int $$3 = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs($$2) < 2 && Math.abs($$3) < 2) continue;
            int $$4 = this.random.nextIntBetweenInclusive(-1, 1);
            if (!this.maybeTeleportTo($$0.getX() + $$2, $$0.getY() + $$4, $$0.getZ() + $$3)) continue;
            return;
        }
    }

    private boolean maybeTeleportTo(int $$0, int $$1, int $$2) {
        if (!this.canTeleportTo(new BlockPos($$0, $$1, $$2))) {
            return false;
        }
        this.snapTo((double)$$0 + 0.5, $$1, (double)$$2 + 0.5, this.getYRot(), this.getXRot());
        this.navigation.stop();
        return true;
    }

    private boolean canTeleportTo(BlockPos $$0) {
        PathType $$1 = WalkNodeEvaluator.getPathTypeStatic(this, $$0);
        if ($$1 != PathType.WALKABLE) {
            return false;
        }
        BlockState $$2 = this.level().getBlockState($$0.below());
        if (!this.canFlyToOwner() && $$2.getBlock() instanceof LeavesBlock) {
            return false;
        }
        BlockPos $$3 = $$0.subtract(this.blockPosition());
        return this.level().noCollision(this, this.getBoundingBox().move($$3));
    }

    public final boolean unableToMoveToOwner() {
        return this.isOrderedToSit() || this.isPassenger() || this.mayBeLeashed() || this.getOwner() != null && this.getOwner().isSpectator();
    }

    protected boolean canFlyToOwner() {
        return false;
    }

    public class TamableAnimalPanicGoal
    extends PanicGoal {
        public TamableAnimalPanicGoal(double $$1, TagKey<DamageType> $$2) {
            super((PathfinderMob)TamableAnimal.this, $$1, $$2);
        }

        public TamableAnimalPanicGoal(double $$1) {
            super(TamableAnimal.this, $$1);
        }

        @Override
        public void tick() {
            if (!TamableAnimal.this.unableToMoveToOwner() && TamableAnimal.this.shouldTryTeleportToOwner()) {
                TamableAnimal.this.tryToTeleportToOwner();
            }
            super.tick();
        }
    }
}

