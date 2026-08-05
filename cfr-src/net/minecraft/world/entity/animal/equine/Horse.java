/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Horse
extends AbstractHorse {
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.HORSE.getHeight() + 0.125f, 0.0f)).scale(0.5f);
    private static final int DEFAULT_VARIANT = 0;

    public Horse(EntityType<? extends Horse> $$0, Level $$1) {
        super((EntityType<? extends AbstractHorse>)$$0, $$1);
        this.setPathfindingMalus(PathType.DANGER_OTHER, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, -1.0f);
    }

    @Override
    protected void randomizeAttributes(RandomSource $$0) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Horse.generateMaxHealth($$0::nextInt));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Horse.generateSpeed($$0::nextDouble));
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Horse.generateJumpStrength($$0::nextDouble));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("Variant", this.getTypeVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setTypeVariant($$0.getIntOr("Variant", 0));
    }

    private void setTypeVariant(int $$0) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, $$0);
    }

    private int getTypeVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariantAndMarkings(Variant $$0, Markings $$1) {
        this.setTypeVariant($$0.getId() & 0xFF | $$1.getId() << 8 & 0xFF00);
    }

    public Variant getVariant() {
        return Variant.byId(this.getTypeVariant() & 0xFF);
    }

    private void setVariant(Variant $$0) {
        this.setTypeVariant($$0.getId() & 0xFF | this.getTypeVariant() & 0xFFFFFF00);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.HORSE_VARIANT) {
            return Horse.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.HORSE_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.HORSE_VARIANT) {
            this.setVariant(Horse.castComponentValue(DataComponents.HORSE_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    public Markings getMarkings() {
        return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
    }

    @Override
    protected void playGallopSound(SoundType $$0) {
        super.playGallopSound($$0);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.HORSE_BREATHE, $$0.getVolume() * 0.6f, $$0.getPitch());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.HORSE_ANGRY;
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        boolean $$2;
        boolean bl = $$2 = !this.isBaby() && this.isTamed() && $$0.isSecondaryUseActive();
        if (this.isVehicle() || $$2) {
            return super.mobInteract($$0, $$1);
        }
        ItemStack $$3 = $$0.getItemInHand($$1);
        if (!$$3.isEmpty()) {
            if (this.isFood($$3)) {
                return this.fedFood($$0, $$3);
            }
            if (!this.isTamed()) {
                this.makeMad();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    public boolean canMate(Animal $$0) {
        if ($$0 == this) {
            return false;
        }
        if ($$0 instanceof Donkey || $$0 instanceof Horse) {
            return this.canParent() && ((AbstractHorse)$$0).canParent();
        }
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        if ($$1 instanceof Donkey) {
            Mule $$2 = EntityType.MULE.create($$0, EntitySpawnReason.BREEDING);
            if ($$2 != null) {
                this.setOffspringAttributes($$1, $$2);
            }
            return $$2;
        }
        Horse $$3 = (Horse)$$1;
        Horse $$4 = EntityType.HORSE.create($$0, EntitySpawnReason.BREEDING);
        if ($$4 != null) {
            Markings $$12;
            Variant $$8;
            int $$5 = this.random.nextInt(9);
            if ($$5 < 4) {
                Variant $$6 = this.getVariant();
            } else if ($$5 < 8) {
                Variant $$7 = $$3.getVariant();
            } else {
                $$8 = Util.getRandom(Variant.values(), this.random);
            }
            int $$9 = this.random.nextInt(5);
            if ($$9 < 2) {
                Markings $$10 = this.getMarkings();
            } else if ($$9 < 4) {
                Markings $$11 = $$3.getMarkings();
            } else {
                $$12 = Util.getRandom(Markings.values(), this.random);
            }
            $$4.setVariantAndMarkings($$8, $$12);
            this.setOffspringAttributes($$1, $$4);
        }
        return $$4;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot $$0) {
        return true;
    }

    @Override
    protected void hurtArmor(DamageSource $$0, float $$1) {
        this.doHurtEquipment($$0, $$1, EquipmentSlot.BODY);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Variant $$6;
        RandomSource $$4 = $$0.getRandom();
        if ($$3 instanceof HorseGroupData) {
            Variant $$5 = ((HorseGroupData)$$3).variant;
        } else {
            $$6 = Util.getRandom(Variant.values(), $$4);
            $$3 = new HorseGroupData($$6);
        }
        this.setVariantAndMarkings($$6, Util.getRandom(Markings.values(), $$4));
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    public static class HorseGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant $$0) {
            super(true);
            this.variant = $$0;
        }
    }
}

