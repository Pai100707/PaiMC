/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Llama
extends AbstractChestedHorse
implements RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.LLAMA.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.LLAMA.getHeight() - 0.8125f, -0.3f)).scale(0.5f);
    boolean didSpit;
    private @Nullable Llama caravanHead;
    private @Nullable Llama caravanTail;

    public Llama(EntityType<? extends Llama> $$0, Level $$1) {
        super((EntityType<? extends AbstractChestedHorse>)$$0, $$1);
        this.getNavigation().setRequiredPathLength(40.0f);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int $$0) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, $$0)));
    }

    private void setRandomStrength(RandomSource $$0) {
        int $$1 = $$0.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + $$0.nextInt($$1));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("Variant", Variant.LEGACY_CODEC, this.getVariant());
        $$0.putInt("Strength", this.getStrength());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setStrength($$0.getIntOr("Strength", 0));
        super.readAdditionalSaveData($$0);
        this.setVariant($$0.read("Variant", Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1f));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Llama.createBaseChestedHorseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_STRENGTH_ID, 0);
        $$0.define(DATA_VARIANT_ID, 0);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    private void setVariant(Variant $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0.id);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.LLAMA_VARIANT) {
            return Llama.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.LLAMA_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.LLAMA_VARIANT) {
            this.setVariant(Llama.castComponentValue(DataComponents.LLAMA_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean handleEating(Player $$0, ItemStack $$1) {
        SoundEvent $$6;
        int $$2 = 0;
        int $$3 = 0;
        float $$4 = 0.0f;
        boolean $$5 = false;
        if ($$1.is(Items.WHEAT)) {
            $$2 = 10;
            $$3 = 3;
            $$4 = 2.0f;
        } else if ($$1.is(Blocks.HAY_BLOCK.asItem())) {
            $$2 = 90;
            $$3 = 6;
            $$4 = 10.0f;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
                $$5 = true;
                this.setInLove($$0);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && $$4 > 0.0f) {
            this.heal($$4);
            $$5 = true;
        }
        if (this.isBaby() && $$2 > 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide()) {
                this.ageUp($$2);
                $$5 = true;
            }
        }
        if (!($$3 <= 0 || !$$5 && this.isTamed() || this.getTemper() >= this.getMaxTemper() || this.level().isClientSide())) {
            this.modifyTemper($$3);
            $$5 = true;
        }
        if ($$5 && !this.isSilent() && ($$6 = this.getEatingSound()) != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return $$5;
    }

    @Override
    public boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Variant $$6;
        RandomSource $$4 = $$0.getRandom();
        this.setRandomStrength($$4);
        if ($$3 instanceof LlamaGroupData) {
            Variant $$5 = ((LlamaGroupData)$$3).variant;
        } else {
            $$6 = Util.getRandom(Variant.values(), $$4);
            $$3 = new LlamaGroupData($$6);
        }
        this.setVariant($$6);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot $$0) {
        return true;
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal $$0) {
        return $$0 != this && $$0 instanceof Llama && this.canParent() && ((Llama)$$0).canParent();
    }

    @Override
    public @Nullable Llama getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Llama $$2 = this.makeNewLlama();
        if ($$2 != null) {
            this.setOffspringAttributes($$1, $$2);
            Llama $$3 = (Llama)$$1;
            int $$4 = this.random.nextInt(Math.max(this.getStrength(), $$3.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03f) {
                ++$$4;
            }
            $$2.setStrength($$4);
            $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : $$3.getVariant());
        }
        return $$2;
    }

    protected @Nullable Llama makeNewLlama() {
        return EntityType.LLAMA.create(this.level(), EntitySpawnReason.BREEDING);
    }

    private void spit(LivingEntity $$0) {
        LlamaSpit $$1 = new LlamaSpit(this.level(), this);
        double $$2 = $$0.getX() - this.getX();
        double $$3 = $$0.getY(0.3333333333333333) - $$1.getY();
        double $$4 = $$0.getZ() - this.getZ();
        double $$5 = Math.sqrt($$2 * $$2 + $$4 * $$4) * (double)0.2f;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$6 = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot($$1, $$6, ItemStack.EMPTY, $$2, $$3 + $$5, $$4, 1.5f, 10.0f);
        }
        if (!this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.didSpit = true;
    }

    void setDidSpit(boolean $$0) {
        this.didSpit = $$0;
    }

    @Override
    public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
        int $$3 = this.calculateFallDamage($$0, $$1);
        if ($$3 <= 0) {
            return false;
        }
        if ($$0 >= 6.0) {
            this.hurt($$2, $$3);
            this.propagateFallToPassengers($$0, $$1, $$2);
        }
        this.playBlockFallSound();
        return true;
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(Llama $$0) {
        this.caravanHead = $$0;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    public @Nullable Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    public boolean supportQuadLeash() {
        return false;
    }

    @Override
    protected void followMommy(ServerLevel $$0) {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy($$0);
        }
    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity $$0, float $$1) {
        this.spit($$0);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        return Llama.getDefaultPassengerAttachmentPoint(this, $$0, $$1.attachments());
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static enum Variant implements StringRepresentable
    {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

        public static final Variant DEFAULT;
        private static final IntFunction<Variant> BY_ID;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> LEGACY_CODEC;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        final int id;
        private final String name;

        private Variant(int $$0, String $$1) {
            this.id = $$0;
            this.name = $$1;
        }

        public int getId() {
            return this.id;
        }

        public static Variant byId(int $$0) {
            return BY_ID.apply($$0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            DEFAULT = CREAMY;
            BY_ID = ByIdMap.continuous(Variant::getId, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::getId);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::getId);
        }
    }

    static class LlamaHurtByTargetGoal
    extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama $$0) {
            super($$0, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            Mob mob = this.mob;
            if (mob instanceof Llama) {
                Llama $$0 = (Llama)mob;
                if ($$0.didSpit) {
                    $$0.setDidSpit(false);
                    return false;
                }
            }
            return super.canContinueToUse();
        }
    }

    static class LlamaAttackWolfGoal
    extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama $$02) {
            super($$02, Wolf.class, 16, false, true, ($$0, $$1) -> !((Wolf)$$0).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        LlamaGroupData(Variant $$0) {
            super(true);
            this.variant = $$0;
        }
    }
}

