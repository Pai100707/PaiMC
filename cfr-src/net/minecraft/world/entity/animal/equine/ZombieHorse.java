/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import java.util.function.DoubleSupplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ZombieHorse
extends AbstractHorse {
    private static final float SPEED_FACTOR = 42.16f;
    private static final double BASE_JUMP_STRENGTH = 0.5;
    private static final double PER_RANDOM_JUMP_STRENGTH = 0.06666666666666667;
    private static final double BASE_SPEED = 9.0;
    private static final double PER_RANDOM_SPEED = 1.0;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIE_HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.ZOMBIE_HORSE.getHeight() - 0.03125f, 0.0f)).scale(0.5f);

    public ZombieHorse(EntityType<? extends ZombieHorse> $$0, Level $$1) {
        super((EntityType<? extends AbstractHorse>)$$0, $$1);
        this.setPathfindingMalus(PathType.DANGER_OTHER, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, -1.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ZombieHorse.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 25.0);
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        this.setPersistenceRequired();
        return super.interact($$0, $$1);
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return true;
    }

    @Override
    public boolean isMobControlled() {
        return this.getFirstPassenger() instanceof Mob;
    }

    @Override
    protected void randomizeAttributes(RandomSource $$0) {
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(ZombieHorse.generateZombieHorseJumpStrength($$0::nextDouble));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(ZombieHorse.generateZombieHorseSpeed($$0::nextDouble));
    }

    private static double generateZombieHorseJumpStrength(DoubleSupplier $$0) {
        return 0.5 + $$0.getAsDouble() * 0.06666666666666667 + $$0.getAsDouble() * 0.06666666666666667 + $$0.getAsDouble() * 0.06666666666666667;
    }

    private static double generateZombieHorseSpeed(DoubleSupplier $$0) {
        return (9.0 + $$0.getAsDouble() * 1.0 + $$0.getAsDouble() * 1.0 + $$0.getAsDouble() * 1.0) / (double)42.16f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ZOMBIE_HORSE_ANGRY;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.ZOMBIE_HORSE_EAT;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return null;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.ZOMBIE_HORSE_FOOD), false));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Zombie $$4;
        if ($$2 == EntitySpawnReason.NATURAL && ($$4 = EntityType.ZOMBIE.create(this.level(), EntitySpawnReason.JOCKEY)) != null) {
            $$4.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
            $$4.finalizeSpawn($$0, $$1, $$2, null);
            $$4.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
            $$4.startRiding(this, false, false);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
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
    public boolean canUseSlot(EquipmentSlot $$0) {
        return true;
    }

    @Override
    public boolean canBeLeashed() {
        return this.isTamed() || !this.isMobControlled();
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.ZOMBIE_HORSE_FOOD);
    }

    @Override
    protected EquipmentSlot sunProtectionSlot() {
        return EquipmentSlot.BODY;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.41, 0.18, 0.73);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    @Override
    public float chargeSpeedModifier() {
        return 1.4f;
    }
}

