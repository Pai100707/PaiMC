/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.sheep;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.SheepColorSpawnRules;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class Sheep
extends Animal
implements Shearable {
    private static final int EAT_ANIMATION_TICKS = 40;
    private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
    private static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;
    private static final boolean DEFAULT_SHEARED = false;
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    public Sheep(EntityType<? extends Sheep> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, $$0 -> $$0.is(ItemTags.SHEEP_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.SHEEP_FOOD);
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep($$0);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }
        super.aiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.23f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_WOOL_ID, (byte)0);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public float getHeadEatPositionScale(float $$0) {
        if (this.eatAnimationTick <= 0) {
            return 0.0f;
        }
        if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0f;
        }
        if (this.eatAnimationTick < 4) {
            return ((float)this.eatAnimationTick - $$0) / 4.0f;
        }
        return -((float)(this.eatAnimationTick - 40) - $$0) / 4.0f;
    }

    public float getHeadEatAngleScale(float $$0) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float $$1 = ((float)(this.eatAnimationTick - 4) - $$0) / 32.0f;
            return 0.62831855f + 0.21991149f * Mth.sin($$1 * 28.7f);
        }
        if (this.eatAnimationTick > 0) {
            return 0.62831855f;
        }
        return this.getXRot($$0) * ((float)Math.PI / 180);
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if ($$2.is(Items.SHEARS)) {
            Level level = this.level();
            if (level instanceof ServerLevel) {
                ServerLevel $$3 = (ServerLevel)level;
                if (this.readyForShearing()) {
                    this.shear($$3, SoundSource.PLAYERS, $$2);
                    this.gameEvent(GameEvent.SHEAR, $$0);
                    $$2.hurtAndBreak(1, (LivingEntity)$$0, $$1.asEquipmentSlot());
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
            return InteractionResult.CONSUME;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    public void shear(ServerLevel $$02, SoundSource $$12, ItemStack $$2) {
        $$02.playSound(null, this, SoundEvents.SHEEP_SHEAR, $$12, 1.0f, 1.0f);
        this.dropFromShearingLootTable($$02, BuiltInLootTables.SHEAR_SHEEP, $$2, ($$0, $$1) -> {
            for (int $$2 = 0; $$2 < $$1.getCount(); ++$$2) {
                ItemEntity $$3 = this.spawnAtLocation((ServerLevel)$$0, $$1.copyWithCount(1), 1.0f);
                if ($$3 == null) continue;
                $$3.setDeltaMovement($$3.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1f, this.random.nextFloat() * 0.05f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f));
            }
        });
        this.setSheared(true);
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("Sheared", this.isSheared());
        $$0.store("Color", DyeColor.LEGACY_ID_CODEC, this.getColor());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setSheared($$0.getBooleanOr("Sheared", false));
        this.setColor($$0.read("Color", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLOR));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15f, 1.0f);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 0xF);
    }

    public void setColor(DyeColor $$0) {
        byte $$1 = this.entityData.get(DATA_WOOL_ID);
        this.entityData.set(DATA_WOOL_ID, (byte)($$1 & 0xF0 | $$0.getId() & 0xF));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.SHEEP_COLOR) {
            return Sheep.castComponentValue($$0, this.getColor());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.SHEEP_COLOR);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.SHEEP_COLOR) {
            this.setColor(Sheep.castComponentValue(DataComponents.SHEEP_COLOR, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    public boolean isSheared() {
        return (this.entityData.get(DATA_WOOL_ID) & 0x10) != 0;
    }

    public void setSheared(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_WOOL_ID);
        if ($$0) {
            this.entityData.set(DATA_WOOL_ID, (byte)($$1 | 0x10));
        } else {
            this.entityData.set(DATA_WOOL_ID, (byte)($$1 & 0xFFFFFFEF));
        }
    }

    public static DyeColor getRandomSheepColor(ServerLevelAccessor $$0, BlockPos $$1) {
        Holder<Biome> $$2 = $$0.getBiome($$1);
        return SheepColorSpawnRules.getSheepColor($$2, $$0.getRandom());
    }

    @Override
    public @Nullable Sheep getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Sheep $$2 = EntityType.SHEEP.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null) {
            DyeColor $$3 = this.getColor();
            DyeColor $$4 = ((Sheep)$$1).getColor();
            $$2.setColor(DyeColor.getMixedColor($$0, $$3, $$4));
        }
        return $$2;
    }

    @Override
    public void ate() {
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        this.setColor(Sheep.getRandomSheepColor($$0, this.blockPosition()));
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

