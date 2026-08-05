/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class AgeableMob
extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
    public static final int BABY_START_AGE = -24000;
    private static final int FORCED_AGE_PARTICLE_TICKS = 40;
    protected static final int DEFAULT_AGE = 0;
    protected static final int DEFAULT_FORCED_AGE = 0;
    protected int age = 0;
    protected int forcedAge = 0;
    protected int forcedAgeTimer;

    protected AgeableMob(EntityType<? extends AgeableMob> $$0, Level $$1) {
        super((EntityType<? extends PathfinderMob>)$$0, $$1);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        AgeableMobGroupData $$4;
        if ($$3 == null) {
            $$3 = new AgeableMobGroupData(true);
        }
        if (($$4 = (AgeableMobGroupData)$$3).isShouldSpawnBaby() && $$4.getGroupSize() > 0 && $$0.getRandom().nextFloat() <= $$4.getBabySpawnChance()) {
            this.setAge(-24000);
        }
        $$4.increaseGroupSizeByOne();
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    public abstract @Nullable AgeableMob getBreedOffspring(ServerLevel var1, AgeableMob var2);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_BABY_ID, false);
    }

    public boolean canBreed() {
        return false;
    }

    public int getAge() {
        if (this.level().isClientSide()) {
            return this.entityData.get(DATA_BABY_ID) != false ? -1 : 1;
        }
        return this.age;
    }

    public void ageUp(int $$0, boolean $$1) {
        int $$2;
        int $$3 = $$2 = this.getAge();
        if (($$2 += $$0 * 20) > 0) {
            $$2 = 0;
        }
        int $$4 = $$2 - $$3;
        this.setAge($$2);
        if ($$1) {
            this.forcedAge += $$4;
            if (this.forcedAgeTimer == 0) {
                this.forcedAgeTimer = 40;
            }
        }
        if (this.getAge() == 0) {
            this.setAge(this.forcedAge);
        }
    }

    public void ageUp(int $$0) {
        this.ageUp($$0, false);
    }

    public void setAge(int $$0) {
        int $$1 = this.getAge();
        this.age = $$0;
        if ($$1 < 0 && $$0 >= 0 || $$1 >= 0 && $$0 < 0) {
            this.entityData.set(DATA_BABY_ID, $$0 < 0);
            this.ageBoundaryReached();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("Age", this.getAge());
        $$0.putInt("ForcedAge", this.forcedAge);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setAge($$0.getIntOr("Age", 0));
        this.forcedAge = $$0.getIntOr("ForcedAge", 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_BABY_ID.equals($$0)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0) {
                    this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
                }
                --this.forcedAgeTimer;
            }
        } else if (this.isAlive()) {
            int $$0 = this.getAge();
            if ($$0 < 0) {
                this.setAge(++$$0);
            } else if ($$0 > 0) {
                this.setAge(--$$0);
            }
        }
    }

    protected void ageBoundaryReached() {
        AbstractBoat $$0;
        Entity entity;
        if (!this.isBaby() && this.isPassenger() && (entity = this.getVehicle()) instanceof AbstractBoat && !($$0 = (AbstractBoat)entity).hasEnoughSpaceFor(this)) {
            this.stopRiding();
        }
    }

    @Override
    public boolean isBaby() {
        return this.getAge() < 0;
    }

    @Override
    public void setBaby(boolean $$0) {
        this.setAge($$0 ? -24000 : 0);
    }

    public static int getSpeedUpSecondsWhenFeeding(int $$0) {
        return (int)((float)($$0 / 20) * 0.1f);
    }

    @VisibleForTesting
    public int getForcedAge() {
        return this.forcedAge;
    }

    @VisibleForTesting
    public int getForcedAgeTimer() {
        return this.forcedAgeTimer;
    }

    public static class AgeableMobGroupData
    implements SpawnGroupData {
        private int groupSize;
        private final boolean shouldSpawnBaby;
        private final float babySpawnChance;

        public AgeableMobGroupData(boolean $$0, float $$1) {
            this.shouldSpawnBaby = $$0;
            this.babySpawnChance = $$1;
        }

        public AgeableMobGroupData(boolean $$0) {
            this($$0, 0.05f);
        }

        public AgeableMobGroupData(float $$0) {
            this(true, $$0);
        }

        public int getGroupSize() {
            return this.groupSize;
        }

        public void increaseGroupSizeByOne() {
            ++this.groupSize;
        }

        public boolean isShouldSpawnBaby() {
            return this.shouldSpawnBaby;
        }

        public float getBabySpawnChance() {
            return this.babySpawnChance;
        }
    }
}

