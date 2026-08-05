/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.fish;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Salmon
extends AbstractSchoolingFish {
    private static final String TAG_TYPE = "type";
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(Salmon.class, EntityDataSerializers.INT);

    public Salmon(EntityType<? extends Salmon> $$0, Level $$1) {
        super((EntityType<? extends AbstractSchoolingFish>)$$0, $$1);
        this.refreshDimensions();
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_TYPE, Variant.DEFAULT.id());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_TYPE.equals($$0)) {
            this.refreshDimensions();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store(TAG_TYPE, Variant.CODEC, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setVariant($$0.read(TAG_TYPE, Variant.CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    public void saveToBucketTag(ItemStack $$0) {
        Bucketable.saveDefaultDataToBucketTag(this, $$0);
        $$0.copyFrom(DataComponents.SALMON_SIZE, this);
    }

    private void setVariant(Variant $$0) {
        this.entityData.set(DATA_TYPE, $$0.id);
    }

    public Variant getVariant() {
        return Variant.BY_ID.apply(this.entityData.get(DATA_TYPE));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.SALMON_SIZE) {
            return Salmon.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.SALMON_SIZE);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.SALMON_SIZE) {
            this.setVariant(Salmon.castComponentValue(DataComponents.SALMON_SIZE, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        WeightedList.Builder<Variant> $$4 = WeightedList.builder();
        $$4.add(Variant.SMALL, 30);
        $$4.add(Variant.MEDIUM, 50);
        $$4.add(Variant.LARGE, 15);
        $$4.build().getRandom(this.random).ifPresent(this::setVariant);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    public float getSalmonScale() {
        return this.getVariant().boundingBoxScale;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose $$0) {
        return super.getDefaultDimensions($$0).scale(this.getSalmonScale());
    }

    public static enum Variant implements StringRepresentable
    {
        SMALL("small", 0, 0.5f),
        MEDIUM("medium", 1, 1.0f),
        LARGE("large", 2, 1.5f);

        public static final Variant DEFAULT;
        public static final StringRepresentable.EnumCodec<Variant> CODEC;
        static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        private final String name;
        final int id;
        final float boundingBoxScale;

        private Variant(String $$0, int $$1, float $$2) {
            this.name = $$0;
            this.id = $$1;
            this.boundingBoxScale = $$2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int id() {
            return this.id;
        }

        static {
            DEFAULT = MEDIUM;
            CODEC = StringRepresentable.fromEnum(Variant::values);
            BY_ID = ByIdMap.continuous(Variant::id, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::id);
        }
    }
}

