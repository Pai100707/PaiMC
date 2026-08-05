/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.cow;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class MushroomCow
extends AbstractCow
implements Shearable {
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.INT);
    private static final int MUTATE_CHANCE = 1024;
    private static final String TAG_STEW_EFFECTS = "stew_effects";
    private @Nullable SuspiciousStewEffects stewEffects;
    private @Nullable UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> $$0, Level $$1) {
        super((EntityType<? extends AbstractCow>)$$0, $$1);
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        if ($$1.getBlockState($$0.below()).is(Blocks.MYCELIUM)) {
            return 10.0f;
        }
        return $$1.getPathfindingCostFromLightLevels($$0);
    }

    public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && MushroomCow.isBrightEnoughToSpawn($$1, $$3);
    }

    @Override
    public void thunderHit(ServerLevel $$0, LightningBolt $$1) {
        UUID $$2 = $$1.getUUID();
        if (!$$2.equals(this.lastLightningBoltUUID)) {
            this.setVariant(this.getVariant() == Variant.RED ? Variant.BROWN : Variant.RED);
            this.lastLightningBoltUUID = $$2;
            this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_TYPE, Variant.DEFAULT.id);
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$2.is(Items.BOWL) && !this.isBaby()) {
            SoundEvent $$8;
            ItemStack $$5;
            boolean $$3 = false;
            if (this.stewEffects != null) {
                $$3 = true;
                ItemStack $$4 = new ItemStack(Items.SUSPICIOUS_STEW);
                $$4.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
                this.stewEffects = null;
            } else {
                $$5 = new ItemStack(Items.MUSHROOM_STEW);
            }
            ItemStack $$6 = ItemUtils.createFilledResult($$2, $$0, $$5, false);
            $$0.setItemInHand($$1, $$6);
            if ($$3) {
                SoundEvent $$7 = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
            } else {
                $$8 = SoundEvents.MOOSHROOM_MILK;
            }
            this.playSound($$8, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if ($$2.is(Items.SHEARS) && this.readyForShearing()) {
            Level $$3 = this.level();
            if ($$3 instanceof ServerLevel) {
                ServerLevel $$9 = (ServerLevel)$$3;
                this.shear($$9, SoundSource.PLAYERS, $$2);
                this.gameEvent(GameEvent.SHEAR, $$0);
                $$2.hurtAndBreak(1, (LivingEntity)$$0, $$1.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        if (this.getVariant() == Variant.BROWN) {
            Optional<SuspiciousStewEffects> $$10 = this.getEffectsFromItemStack($$2);
            if ($$10.isEmpty()) {
                return super.mobInteract($$0, $$1);
            }
            if (this.stewEffects != null) {
                for (int $$11 = 0; $$11 < 2; ++$$11) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0, this.getY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
            } else {
                $$2.consume(1, $$0);
                SpellParticleOption $$12 = SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0f);
                for (int $$13 = 0; $$13 < 4; ++$$13) {
                    this.level().addParticle($$12, this.getX() + this.random.nextDouble() / 2.0, this.getY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
                this.stewEffects = $$10.get();
                this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$22) {
        $$0.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, $$1, 1.0f, 1.0f);
        this.convertTo(EntityType.COW, ConversionParams.single(this, false, false), $$2 -> {
            $$0.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.dropFromShearingLootTable($$0, BuiltInLootTables.SHEAR_MOOSHROOM, $$22, ($$0, $$1) -> {
                for (Mob $$2 = 0; $$2 < $$1.getCount(); ++$$2) {
                    $$0.addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(1.0), this.getZ(), $$1.copyWithCount(1)));
                }
            });
        });
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Type", Variant.CODEC, this.getVariant());
        $$0.storeNullable(TAG_STEW_EFFECTS, SuspiciousStewEffects.CODEC, this.stewEffects);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setVariant($$0.read("Type", Variant.CODEC).orElse(Variant.DEFAULT));
        this.stewEffects = $$0.read(TAG_STEW_EFFECTS, SuspiciousStewEffects.CODEC).orElse(null);
    }

    private Optional<SuspiciousStewEffects> getEffectsFromItemStack(ItemStack $$0) {
        SuspiciousEffectHolder $$1 = SuspiciousEffectHolder.tryGet($$0.getItem());
        if ($$1 != null) {
            return Optional.of($$1.getSuspiciousEffects());
        }
        return Optional.empty();
    }

    private void setVariant(Variant $$0) {
        this.entityData.set(DATA_TYPE, $$0.id);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.MOOSHROOM_VARIANT) {
            return MushroomCow.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.MOOSHROOM_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.MOOSHROOM_VARIANT) {
            this.setVariant(MushroomCow.castComponentValue(DataComponents.MOOSHROOM_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public @Nullable MushroomCow getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        MushroomCow $$2 = EntityType.MOOSHROOM.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null) {
            $$2.setVariant(this.getOffspringVariant((MushroomCow)$$1));
        }
        return $$2;
    }

    private Variant getOffspringVariant(MushroomCow $$0) {
        Variant $$4;
        Variant $$2;
        Variant $$1 = this.getVariant();
        if ($$1 == ($$2 = $$0.getVariant()) && this.random.nextInt(1024) == 0) {
            Variant $$3 = $$1 == Variant.BROWN ? Variant.RED : Variant.BROWN;
        } else {
            $$4 = this.random.nextBoolean() ? $$1 : $$2;
        }
        return $$4;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static enum Variant implements StringRepresentable
    {
        RED("red", 0, Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", 1, Blocks.BROWN_MUSHROOM.defaultBlockState());

        public static final Variant DEFAULT;
        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        private final String type;
        final int id;
        private final BlockState blockState;

        private Variant(String $$0, int $$1, BlockState $$2) {
            this.type = $$0;
            this.id = $$1;
            this.blockState = $$2;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        @Override
        public String getSerializedName() {
            return this.type;
        }

        private int id() {
            return this.id;
        }

        static Variant byId(int $$0) {
            return BY_ID.apply($$0);
        }

        static {
            DEFAULT = RED;
            CODEC = StringRepresentable.fromEnum(Variant::values);
            BY_ID = ByIdMap.continuous(Variant::id, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::id);
        }
    }
}

