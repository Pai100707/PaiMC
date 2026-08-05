/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.wolf;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Wolf
extends TamableAnimal
implements NeutralMob {
    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_ANGER_END_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Holder<WolfVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_VARIANT);
    private static final EntityDataAccessor<Holder<WolfSoundVariant>> DATA_SOUND_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_SOUND_VARIANT);
    public static final TargetingConditions.Selector PREY_SELECTOR = ($$0, $$1) -> {
        EntityType<?> $$2 = $$0.getType();
        return $$2 == EntityType.SHEEP || $$2 == EntityType.RABBIT || $$2 == EntityType.FOX;
    };
    private static final float START_HEALTH = 8.0f;
    private static final float TAME_HEALTH = 40.0f;
    private static final float ARMOR_REPAIR_UNIT = 0.125f;
    public static final float DEFAULT_TAIL_ANGLE = 0.62831855f;
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;

    public Wolf(EntityType<? extends Wolf> $$0, Level $$1) {
        super((EntityType<? extends TamableAnimal>)$$0, $$1);
        this.setTame(false, false);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0f);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new WolfAvoidEntityGoal<Llama>(this, Llama.class, 24.0f, 1.5, 1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<Animal>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<Turtle>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<AbstractSkeleton>((Mob)this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<Wolf>(this, true));
    }

    public Identifier getTexture() {
        WolfVariant $$0 = this.getVariant().value();
        if (this.isTame()) {
            return $$0.assetInfo().tame().texturePath();
        }
        if (this.isAngry()) {
            return $$0.assetInfo().angry().texturePath();
        }
        return $$0.assetInfo().wild().texturePath();
    }

    private Holder<WolfVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    private void setVariant(Holder<WolfVariant> $$0) {
        this.entityData.set(DATA_VARIANT_ID, $$0);
    }

    private Holder<WolfSoundVariant> getSoundVariant() {
        return this.entityData.get(DATA_SOUND_VARIANT_ID);
    }

    private void setSoundVariant(Holder<WolfSoundVariant> $$0) {
        this.entityData.set(DATA_SOUND_VARIANT_ID, $$0);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.WOLF_VARIANT) {
            return Wolf.castComponentValue($$0, this.getVariant());
        }
        if ($$0 == DataComponents.WOLF_SOUND_VARIANT) {
            return Wolf.castComponentValue($$0, this.getSoundVariant());
        }
        if ($$0 == DataComponents.WOLF_COLLAR) {
            return Wolf.castComponentValue($$0, this.getCollarColor());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_VARIANT);
        this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_SOUND_VARIANT);
        this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_COLLAR);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.WOLF_VARIANT) {
            this.setVariant(Wolf.castComponentValue(DataComponents.WOLF_VARIANT, $$1));
            return true;
        }
        if ($$0 == DataComponents.WOLF_SOUND_VARIANT) {
            this.setSoundVariant(Wolf.castComponentValue(DataComponents.WOLF_SOUND_VARIANT, $$1));
            return true;
        }
        if ($$0 == DataComponents.WOLF_COLLAR) {
            this.setCollarColor(Wolf.castComponentValue(DataComponents.WOLF_COLLAR, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        HolderLookup.RegistryLookup $$1 = this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT);
        $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), WolfVariants.DEFAULT));
        $$0.define(DATA_SOUND_VARIANT_ID, (Holder)$$1.get(WolfSoundVariants.CLASSIC).or(((Registry)$$1)::getAny).orElseThrow());
        $$0.define(DATA_INTERESTED_ID, false);
        $$0.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
        $$0.define(DATA_ANGER_END_TIME, -1L);
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("CollarColor", DyeColor.LEGACY_ID_CODEC, this.getCollarColor());
        VariantUtils.writeVariant($$0, this.getVariant());
        this.addPersistentAngerSaveData($$0);
        this.getSoundVariant().unwrapKey().ifPresent($$1 -> $$0.store("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT), $$1));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$02) {
        super.readAdditionalSaveData($$02);
        VariantUtils.readVariant($$02, Registries.WOLF_VARIANT).ifPresent(this::setVariant);
        this.setCollarColor($$02.read("CollarColor", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLLAR_COLOR));
        this.readPersistentAngerSaveData(this.level(), $$02);
        $$02.read("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT)).flatMap($$0 -> this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT).get((ResourceKey)$$0)).ifPresent(this::setSoundVariant);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        if ($$3 instanceof WolfPackData) {
            WolfPackData $$4 = (WolfPackData)$$3;
            this.setVariant($$4.type);
        } else {
            Optional $$5 = VariantUtils.selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.WOLF_VARIANT);
            if ($$5.isPresent()) {
                this.setVariant($$5.get());
                $$3 = new WolfPackData($$5.get());
            }
        }
        this.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), $$0.getRandom()));
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return this.getSoundVariant().value().growlSound().value();
        }
        if (this.random.nextInt(3) == 0) {
            if (this.isTame() && this.getHealth() < 20.0f) {
                return this.getSoundVariant().value().whineSound().value();
            }
            return this.getSoundVariant().value().pantSound().value();
        }
        return this.getSoundVariant().value().ambientSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.canArmorAbsorb($$0)) {
            return SoundEvents.WOLF_ARMOR_DAMAGE;
        }
        return this.getSoundVariant().value().hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getSoundVariant().value().deathSound().value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
            this.level().broadcastEntityEvent(this, (byte)8);
        }
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        this.interestedAngleO = this.interestedAngle;
        this.interestedAngle = this.isInterested() ? (this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f) : (this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f);
        if (this.isInWaterOrRain()) {
            this.isWet = true;
            if (this.isShaking && !this.level().isClientSide()) {
                this.level().broadcastEntityEvent(this, (byte)56);
                this.cancelShake();
            }
        } else if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0f) {
                this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05f;
            if (this.shakeAnimO >= 2.0f) {
                this.isWet = false;
                this.isShaking = false;
                this.shakeAnimO = 0.0f;
                this.shakeAnim = 0.0f;
            }
            if (this.shakeAnim > 0.4f) {
                float $$0 = (float)this.getY();
                int $$1 = (int)(Mth.sin((this.shakeAnim - 0.4f) * (float)Math.PI) * 7.0f);
                Vec3 $$2 = this.getDeltaMovement();
                for (int $$3 = 0; $$3 < $$1; ++$$3) {
                    float $$4 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    float $$5 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)$$4, $$0 + 0.8f, this.getZ() + (double)$$5, $$2.x, $$2.y, $$2.z);
                }
            }
        }
    }

    private void cancelShake() {
        this.isShaking = false;
        this.shakeAnim = 0.0f;
        this.shakeAnimO = 0.0f;
    }

    @Override
    public void die(DamageSource $$0) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0f;
        this.shakeAnim = 0.0f;
        super.die($$0);
    }

    public float getWetShade(float $$0) {
        if (!this.isWet) {
            return 1.0f;
        }
        return Math.min(0.75f + Mth.lerp($$0, this.shakeAnimO, this.shakeAnim) / 2.0f * 0.25f, 1.0f);
    }

    public float getShakeAnim(float $$0) {
        return Mth.lerp($$0, this.shakeAnimO, this.shakeAnim);
    }

    public float getHeadRollAngle(float $$0) {
        return Mth.lerp($$0, this.interestedAngleO, this.interestedAngle) * 0.15f * (float)Math.PI;
    }

    @Override
    public int getMaxHeadXRot() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxHeadXRot();
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        this.setOrderedToSit(false);
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (!this.canArmorAbsorb($$1)) {
            super.actuallyHurt($$0, $$1, $$2);
            return;
        }
        ItemStack $$3 = this.getBodyArmorItem();
        int $$4 = $$3.getDamageValue();
        int $$5 = $$3.getMaxDamage();
        $$3.hurtAndBreak(Mth.ceil($$2), (LivingEntity)this, EquipmentSlot.BODY);
        if (Crackiness.WOLF_ARMOR.byDamage($$4, $$5) != Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
            this.playSound(SoundEvents.WOLF_ARMOR_CRACK);
            $$0.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()), this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
        }
    }

    private boolean canArmorAbsorb(DamageSource $$0) {
        return this.getBodyArmorItem().is(Items.WOLF_ARMOR) && !$$0.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0f);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0);
        }
    }

    @Override
    protected void hurtArmor(DamageSource $$0, float $$1) {
        this.doHurtEquipment($$0, $$1, EquipmentSlot.BODY);
    }

    @Override
    protected boolean canShearEquipment(Player $$0) {
        return this.isOwnedBy($$0);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        Item $$3 = $$2.getItem();
        if (this.isTame()) {
            if (this.isFood($$2) && this.getHealth() < this.getMaxHealth()) {
                this.usePlayerItem($$0, $$1, $$2);
                FoodProperties $$4 = $$2.get(DataComponents.FOOD);
                float $$5 = $$4 != null ? (float)$$4.nutrition() : 1.0f;
                this.heal(2.0f * $$5);
                return InteractionResult.SUCCESS;
            }
            if ($$3 instanceof DyeItem) {
                DyeItem $$6 = (DyeItem)$$3;
                if (this.isOwnedBy($$0)) {
                    DyeColor $$7 = $$6.getDyeColor();
                    if ($$7 == this.getCollarColor()) return super.mobInteract($$0, $$1);
                    this.setCollarColor($$7);
                    $$2.consume(1, $$0);
                    return InteractionResult.SUCCESS;
                }
            }
            if (this.isEquippableInSlot($$2, EquipmentSlot.BODY) && !this.isWearingBodyArmor() && this.isOwnedBy($$0) && !this.isBaby()) {
                this.setBodyArmorItem($$2.copyWithCount(1));
                $$2.consume(1, $$0);
                return InteractionResult.SUCCESS;
            }
            if (this.isInSittingPose() && this.isWearingBodyArmor() && this.isOwnedBy($$0) && this.getBodyArmorItem().isDamaged() && this.getBodyArmorItem().isValidRepairItem($$2)) {
                $$2.shrink(1);
                this.playSound(SoundEvents.WOLF_ARMOR_REPAIR);
                ItemStack $$8 = this.getBodyArmorItem();
                int $$9 = (int)((float)$$8.getMaxDamage() * 0.125f);
                $$8.setDamageValue(Math.max(0, $$8.getDamageValue() - $$9));
                return InteractionResult.SUCCESS;
            }
            InteractionResult $$10 = super.mobInteract($$0, $$1);
            if ($$10.consumesAction() || !this.isOwnedBy($$0)) return $$10;
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS.withoutItem();
        }
        if (this.level().isClientSide() || !$$2.is(Items.BONE) || this.isAngry()) return super.mobInteract($$0, $$1);
        $$2.consume(1, $$0);
        this.tryToTame($$0);
        return InteractionResult.SUCCESS_SERVER;
    }

    private void tryToTame(Player $$0) {
        if (this.random.nextInt(3) == 0) {
            this.tame($$0);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
        } else if ($$0 == 56) {
            this.cancelShake();
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804f;
        }
        if (this.isTame()) {
            float $$0 = this.getMaxHealth();
            float $$1 = ($$0 - this.getHealth()) / $$0;
            return (0.55f - $$1 * 0.4f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.WOLF_FOOD);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.entityData.get(DATA_ANGER_END_TIME);
    }

    @Override
    public void setPersistentAngerEndTime(long $$0) {
        this.entityData.set(DATA_ANGER_END_TIME, $$0);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> $$0) {
        this.persistentAngerTarget = $$0;
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor $$0) {
        this.entityData.set(DATA_COLLAR_COLOR, $$0.getId());
    }

    @Override
    public @Nullable Wolf getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        Wolf $$2 = EntityType.WOLF.create($$0, EntitySpawnReason.BREEDING);
        if ($$2 != null && $$1 instanceof Wolf) {
            Wolf $$3 = (Wolf)$$1;
            if (this.random.nextBoolean()) {
                $$2.setVariant(this.getVariant());
            } else {
                $$2.setVariant($$3.getVariant());
            }
            if (this.isTame()) {
                $$2.setOwnerReference(this.getOwnerReference());
                $$2.setTame(true, true);
                DyeColor $$4 = this.getCollarColor();
                DyeColor $$5 = $$3.getCollarColor();
                $$2.setCollarColor(DyeColor.getMixedColor($$0, $$4, $$5));
            }
            $$2.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), this.random));
        }
        return $$2;
    }

    public void setIsInterested(boolean $$0) {
        this.entityData.set(DATA_INTERESTED_ID, $$0);
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean canMate(Animal $$0) {
        void $$2;
        if ($$0 == this) {
            return false;
        }
        if (!this.isTame()) {
            return false;
        }
        if (!($$0 instanceof Wolf)) {
            return false;
        }
        Wolf $$1 = (Wolf)$$0;
        if (!$$2.isTame()) {
            return false;
        }
        if ($$2.isInSittingPose()) {
            return false;
        }
        return this.isInLove() && $$2.isInLove();
    }

    public boolean isInterested() {
        return this.entityData.get(DATA_INTERESTED_ID);
    }

    @Override
    public boolean wantsToAttack(LivingEntity $$0, LivingEntity $$1) {
        TamableAnimal $$6;
        AbstractHorse $$5;
        if ($$0 instanceof Creeper || $$0 instanceof Ghast || $$0 instanceof ArmorStand) {
            return false;
        }
        if ($$0 instanceof Wolf) {
            Wolf $$2 = (Wolf)$$0;
            return !$$2.isTame() || $$2.getOwner() != $$1;
        }
        if ($$0 instanceof Player) {
            Player $$4;
            Player $$3 = (Player)$$0;
            if ($$1 instanceof Player && !($$4 = (Player)$$1).canHarmPlayer($$3)) {
                return false;
            }
        }
        if ($$0 instanceof AbstractHorse && ($$5 = (AbstractHorse)$$0).isTamed()) {
            return false;
        }
        return !($$0 instanceof TamableAnimal) || !($$6 = (TamableAnimal)$$0).isTame();
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isAngry();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    public static boolean checkWolfSpawnRules(EntityType<Wolf> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && Wolf.isBrightEnoughToSpawn($$1, $$3);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class WolfAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(Wolf $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
            super($$0, $$1, $$2, $$3, $$4);
            this.wolf = $$0;
        }

        @Override
        public boolean canUse() {
            if (super.canUse() && this.toAvoid instanceof Llama) {
                return !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid);
            }
            return false;
        }

        private boolean avoidLlama(Llama $$0) {
            return $$0.getStrength() >= Wolf.this.random.nextInt(5);
        }

        @Override
        public void start() {
            Wolf.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            Wolf.this.setTarget(null);
            super.tick();
        }
    }

    public static class WolfPackData
    extends AgeableMob.AgeableMobGroupData {
        public final Holder<WolfVariant> type;

        public WolfPackData(Holder<WolfVariant> $$0) {
            super(false);
            this.type = $$0;
        }
    }
}

