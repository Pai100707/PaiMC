/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.nautilus;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusAi;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariants;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.item.EitherHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class ZombieNautilus
extends AbstractNautilus {
    private static final EntityDataAccessor<Holder<ZombieNautilusVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(ZombieNautilus.class, EntityDataSerializers.ZOMBIE_NAUTILUS_VARIANT);

    public ZombieNautilus(EntityType<? extends ZombieNautilus> $$0, Level $$1) {
        super((EntityType<? extends AbstractNautilus>)$$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractNautilus.createAttributes().add(Attributes.MOVEMENT_SPEED, 1.1f);
    }

    @Override
    public @Nullable ZombieNautilus getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return null;
    }

    @Override
    protected EquipmentSlot sunProtectionSlot() {
        return EquipmentSlot.BODY;
    }

    protected Brain.Provider<ZombieNautilus> brainProvider() {
        return ZombieNautilusAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return ZombieNautilusAi.makeBrain(this.brainProvider().makeBrain($$0));
    }

    public Brain<ZombieNautilus> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("zombieNautilusBrain");
        this.getBrain().tick($$0, this);
        $$1.pop();
        $$1.push("zombieNautilusActivityUpdate");
        ZombieNautilusAi.updateActivity(this);
        $$1.pop();
        super.customServerAiStep($$0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isUnderWater() ? SoundEvents.ZOMBIE_NAUTILUS_AMBIENT : SoundEvents.ZOMBIE_NAUTILUS_AMBIENT_ON_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return this.isUnderWater() ? SoundEvents.ZOMBIE_NAUTILUS_HURT : SoundEvents.ZOMBIE_NAUTILUS_HURT_ON_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isUnderWater() ? SoundEvents.ZOMBIE_NAUTILUS_DEATH : SoundEvents.ZOMBIE_NAUTILUS_DEATH_ON_LAND;
    }

    @Override
    protected SoundEvent getDashSound() {
        return this.isUnderWater() ? SoundEvents.ZOMBIE_NAUTILUS_DASH : SoundEvents.ZOMBIE_NAUTILUS_DASH_ON_LAND;
    }

    @Override
    protected SoundEvent getDashReadySound() {
        return this.isUnderWater() ? SoundEvents.ZOMBIE_NAUTILUS_DASH_READY : SoundEvents.ZOMBIE_NAUTILUS_DASH_READY_ON_LAND;
    }

    @Override
    protected void playEatingSound() {
        this.makeSound(SoundEvents.ZOMBIE_NAUTILUS_EAT);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ZOMBIE_NAUTILUS_SWIM;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), ZombieNautilusVariants.TEMPERATE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        VariantUtils.readVariant($$0, Registries.ZOMBIE_NAUTILUS_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        VariantUtils.writeVariant($$0, this.getVariant());
    }

    public void setVariant(Holder<ZombieNautilusVariant> $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0);
    }

    public Holder<ZombieNautilusVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.ZOMBIE_NAUTILUS_VARIANT) {
            return ZombieNautilus.castComponentValue($$0, new EitherHolder<ZombieNautilusVariant>(this.getVariant()));
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.ZOMBIE_NAUTILUS_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.ZOMBIE_NAUTILUS_VARIANT) {
            Optional<Holder<ZombieNautilusVariant>> $$2 = ZombieNautilus.castComponentValue(DataComponents.ZOMBIE_NAUTILUS_VARIANT, $$1).unwrap(this.registryAccess());
            if ($$2.isPresent()) {
                this.setVariant($$2.get());
                return true;
            }
            return false;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        VariantUtils.selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.ZOMBIE_NAUTILUS_VARIANT).ifPresent(this::setVariant);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isAggravated() && !this.isMobControlled();
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

