/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnderMan
extends Monster
implements NeutralMob {
    private static final Identifier SPEED_MODIFIER_ATTACKING_ID = Identifier.withDefaultNamespace("attacking");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_ID, 0.15f, AttributeModifier.Operation.ADD_VALUE);
    private static final int DELAY_BETWEEN_CREEPY_STARE_SOUND = 400;
    private static final int MIN_DEAGGRESSION_TIME = 600;
    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Boolean> DATA_CREEPY = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_STARED_AT = SynchedEntityData.defineId(EnderMan.class, EntityDataSerializers.BOOLEAN);
    private int lastStareSound = Integer.MIN_VALUE;
    private int targetChangeTime;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private long persistentAngerEndTime;
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;

    public EnderMan(EntityType<? extends EnderMan> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EndermanFreezeWhenLookedAt(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0, 0.0f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(10, new EndermanLeaveBlockGoal(this));
        this.goalSelector.addGoal(11, new EndermanTakeBlockGoal(this));
        this.targetSelector.addGoal(1, new EndermanLookForPlayerGoal(this, this::isAngryAt));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Endermite>((Mob)this, Endermite.class, true, false));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<EnderMan>(this, false));
    }

    @Override
    public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
        return 0.0f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 7.0).add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public void setTarget(@Nullable LivingEntity $$0) {
        super.setTarget($$0);
        AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if ($$0 == null) {
            this.targetChangeTime = 0;
            this.entityData.set(DATA_CREEPY, false);
            this.entityData.set(DATA_STARED_AT, false);
            $$1.removeModifier(SPEED_MODIFIER_ATTACKING_ID);
        } else {
            this.targetChangeTime = this.tickCount;
            this.entityData.set(DATA_CREEPY, true);
            if (!$$1.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                $$1.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_CARRY_STATE, Optional.empty());
        $$0.define(DATA_CREEPY, false);
        $$0.define(DATA_STARED_AT, false);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setPersistentAngerEndTime(long $$0) {
        this.persistentAngerEndTime = $$0;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> $$0) {
        this.persistentAngerTarget = $$0;
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void playStareSound() {
        if (this.tickCount >= this.lastStareSound + 400) {
            this.lastStareSound = this.tickCount;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5f, 1.0f, false);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_CREEPY.equals($$0) && this.hasBeenStaredAt() && this.level().isClientSide()) {
            this.playStareSound();
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        BlockState $$1 = this.getCarriedBlock();
        if ($$1 != null) {
            $$0.store("carriedBlockState", BlockState.CODEC, $$1);
        }
        this.addPersistentAngerSaveData($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$02) {
        super.readAdditionalSaveData($$02);
        this.setCarriedBlock($$02.read("carriedBlockState", BlockState.CODEC).filter($$0 -> !$$0.isAir()).orElse(null));
        this.readPersistentAngerSaveData(this.level(), $$02);
    }

    boolean isBeingStaredBy(Player $$0) {
        if (!LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM.test($$0)) {
            return false;
        }
        return this.isLookingAtMe($$0, 0.025, true, false, this.getEyeY());
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide()) {
            for (int $$0 = 0; $$0 < 2; ++$$0) {
                this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
            }
        }
        this.jumping = false;
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
        super.aiStep();
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        float $$1;
        if ($$0.isBrightOutside() && this.tickCount >= this.targetChangeTime + 600 && ($$1 = this.getLightLevelDependentMagicValue()) > 0.5f && $$0.canSeeSky(this.blockPosition()) && this.random.nextFloat() * 30.0f < ($$1 - 0.4f) * 2.0f) {
            this.setTarget(null);
            this.teleport();
        }
        super.customServerAiStep($$0);
    }

    protected boolean teleport() {
        if (this.level().isClientSide() || !this.isAlive()) {
            return false;
        }
        double $$0 = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        double $$1 = this.getY() + (double)(this.random.nextInt(64) - 32);
        double $$2 = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.teleport($$0, $$1, $$2);
    }

    boolean teleportTowards(Entity $$0) {
        Vec3 $$1 = new Vec3(this.getX() - $$0.getX(), this.getY(0.5) - $$0.getEyeY(), this.getZ() - $$0.getZ());
        $$1 = $$1.normalize();
        double $$2 = 16.0;
        double $$3 = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - $$1.x * 16.0;
        double $$4 = this.getY() + (double)(this.random.nextInt(16) - 8) - $$1.y * 16.0;
        double $$5 = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - $$1.z * 16.0;
        return this.teleport($$3, $$4, $$5);
    }

    private boolean teleport(double $$0, double $$1, double $$2) {
        BlockPos.MutableBlockPos $$3 = new BlockPos.MutableBlockPos($$0, $$1, $$2);
        while ($$3.getY() > this.level().getMinY() && !this.level().getBlockState($$3).blocksMotion()) {
            $$3.move(Direction.DOWN);
        }
        BlockState $$4 = this.level().getBlockState($$3);
        boolean $$5 = $$4.blocksMotion();
        boolean $$6 = $$4.getFluidState().is(FluidTags.WATER);
        if (!$$5 || $$6) {
            return false;
        }
        Vec3 $$7 = this.position();
        boolean $$8 = this.randomTeleport($$0, $$1, $$2, true);
        if ($$8) {
            this.level().gameEvent(GameEvent.TELEPORT, $$7, GameEvent.Context.of(this));
            if (!this.isSilent()) {
                this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0f, 1.0f);
                this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
        return $$8;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel $$0, DamageSource $$1, boolean $$2) {
        super.dropCustomDeathLoot($$0, $$1, $$2);
        BlockState $$3 = this.getCarriedBlock();
        if ($$3 != null) {
            ItemStack $$4 = new ItemStack(Items.DIAMOND_AXE);
            EnchantmentHelper.enchantItemFromProvider($$4, $$0.registryAccess(), VanillaEnchantmentProviders.ENDERMAN_LOOT_DROP, $$0.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
            LootParams.Builder $$5 = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, $$4).withOptionalParameter(LootContextParams.THIS_ENTITY, this);
            List<ItemStack> $$6 = $$3.getDrops($$5);
            for (ItemStack $$7 : $$6) {
                this.spawnAtLocation($$0, $$7);
            }
        }
    }

    public void setCarriedBlock(@Nullable BlockState $$0) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable($$0));
    }

    public @Nullable BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRY_STATE).orElse(null);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        AbstractThrownPotion $$3;
        AbstractThrownPotion $$4;
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        Entity entity = $$1.getDirectEntity();
        AbstractThrownPotion abstractThrownPotion = $$4 = entity instanceof AbstractThrownPotion ? ($$3 = (AbstractThrownPotion)entity) : null;
        if ($$1.is(DamageTypeTags.IS_PROJECTILE) || $$4 != null) {
            boolean $$5 = $$4 != null && this.hurtWithCleanWater($$0, $$1, $$4, $$2);
            for (int $$6 = 0; $$6 < 64; ++$$6) {
                if (!this.teleport()) continue;
                return true;
            }
            return $$5;
        }
        boolean $$7 = super.hurtServer($$0, $$1, $$2);
        if (!($$1.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
            this.teleport();
        }
        return $$7;
    }

    private boolean hurtWithCleanWater(ServerLevel $$0, DamageSource $$1, AbstractThrownPotion $$2, float $$3) {
        ItemStack $$4 = $$2.getItem();
        PotionContents $$5 = $$4.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if ($$5.is(Potions.WATER)) {
            return super.hurtServer($$0, $$1, $$3);
        }
        return false;
    }

    public boolean isCreepy() {
        return this.entityData.get(DATA_CREEPY);
    }

    public boolean hasBeenStaredAt() {
        return this.entityData.get(DATA_STARED_AT);
    }

    public void setBeingStaredAt() {
        this.entityData.set(DATA_STARED_AT, true);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.getCarriedBlock() != null;
    }

    static class EndermanFreezeWhenLookedAt
    extends Goal {
        private final EnderMan enderman;
        private @Nullable LivingEntity target;

        public EndermanFreezeWhenLookedAt(EnderMan $$0) {
            this.enderman = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        /*
         * WARNING - void declaration
         */
        @Override
        public boolean canUse() {
            void $$1;
            this.target = this.enderman.getTarget();
            LivingEntity livingEntity = this.target;
            if (!(livingEntity instanceof Player)) {
                return false;
            }
            Player $$0 = (Player)livingEntity;
            double $$2 = this.target.distanceToSqr(this.enderman);
            if ($$2 > 256.0) {
                return false;
            }
            return this.enderman.isBeingStaredBy((Player)$$1);
        }

        @Override
        public void start() {
            this.enderman.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
        }
    }

    static class EndermanLeaveBlockGoal
    extends Goal {
        private final EnderMan enderman;

        public EndermanLeaveBlockGoal(EnderMan $$0) {
            this.enderman = $$0;
        }

        @Override
        public boolean canUse() {
            if (this.enderman.getCarriedBlock() == null) {
                return false;
            }
            if (!EndermanLeaveBlockGoal.getServerLevel(this.enderman).getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                return false;
            }
            return this.enderman.getRandom().nextInt(EndermanLeaveBlockGoal.reducedTickDelay(2000)) == 0;
        }

        @Override
        public void tick() {
            RandomSource $$0 = this.enderman.getRandom();
            Level $$1 = this.enderman.level();
            int $$2 = Mth.floor(this.enderman.getX() - 1.0 + $$0.nextDouble() * 2.0);
            int $$3 = Mth.floor(this.enderman.getY() + $$0.nextDouble() * 2.0);
            int $$4 = Mth.floor(this.enderman.getZ() - 1.0 + $$0.nextDouble() * 2.0);
            BlockPos $$5 = new BlockPos($$2, $$3, $$4);
            BlockState $$6 = $$1.getBlockState($$5);
            BlockPos $$7 = $$5.below();
            BlockState $$8 = $$1.getBlockState($$7);
            BlockState $$9 = this.enderman.getCarriedBlock();
            if ($$9 == null) {
                return;
            }
            if (this.canPlaceBlock($$1, $$5, $$9 = Block.updateFromNeighbourShapes($$9, this.enderman.level(), $$5), $$6, $$8, $$7)) {
                $$1.setBlock($$5, $$9, 3);
                $$1.gameEvent(GameEvent.BLOCK_PLACE, $$5, GameEvent.Context.of(this.enderman, $$9));
                this.enderman.setCarriedBlock(null);
            }
        }

        private boolean canPlaceBlock(Level $$0, BlockPos $$1, BlockState $$2, BlockState $$3, BlockState $$4, BlockPos $$5) {
            return $$3.isAir() && !$$4.isAir() && !$$4.is(Blocks.BEDROCK) && $$4.isCollisionShapeFullBlock($$0, $$5) && $$2.canSurvive($$0, $$1) && $$0.getEntities(this.enderman, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf($$1))).isEmpty();
        }
    }

    static class EndermanTakeBlockGoal
    extends Goal {
        private final EnderMan enderman;

        public EndermanTakeBlockGoal(EnderMan $$0) {
            this.enderman = $$0;
        }

        @Override
        public boolean canUse() {
            if (this.enderman.getCarriedBlock() != null) {
                return false;
            }
            if (!EndermanTakeBlockGoal.getServerLevel(this.enderman).getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                return false;
            }
            return this.enderman.getRandom().nextInt(EndermanTakeBlockGoal.reducedTickDelay(20)) == 0;
        }

        @Override
        public void tick() {
            RandomSource $$0 = this.enderman.getRandom();
            Level $$1 = this.enderman.level();
            int $$2 = Mth.floor(this.enderman.getX() - 2.0 + $$0.nextDouble() * 4.0);
            int $$3 = Mth.floor(this.enderman.getY() + $$0.nextDouble() * 3.0);
            int $$4 = Mth.floor(this.enderman.getZ() - 2.0 + $$0.nextDouble() * 4.0);
            BlockPos $$5 = new BlockPos($$2, $$3, $$4);
            BlockState $$6 = $$1.getBlockState($$5);
            Vec3 $$7 = new Vec3((double)this.enderman.getBlockX() + 0.5, (double)$$3 + 0.5, (double)this.enderman.getBlockZ() + 0.5);
            Vec3 $$8 = new Vec3((double)$$2 + 0.5, (double)$$3 + 0.5, (double)$$4 + 0.5);
            BlockHitResult $$9 = $$1.clip(new ClipContext($$7, $$8, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.enderman));
            boolean $$10 = $$9.getBlockPos().equals($$5);
            if ($$6.is(BlockTags.ENDERMAN_HOLDABLE) && $$10) {
                $$1.removeBlock($$5, false);
                $$1.gameEvent(GameEvent.BLOCK_DESTROY, $$5, GameEvent.Context.of(this.enderman, $$6));
                this.enderman.setCarriedBlock($$6.getBlock().defaultBlockState());
            }
        }
    }

    static class EndermanLookForPlayerGoal
    extends NearestAttackableTargetGoal<Player> {
        private final EnderMan enderman;
        private @Nullable Player pendingTarget;
        private int aggroTime;
        private int teleportTime;
        private final TargetingConditions startAggroTargetConditions;
        private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
        private final TargetingConditions.Selector isAngerInducing;

        public EndermanLookForPlayerGoal(EnderMan $$0, @Nullable TargetingConditions.Selector $$12) {
            super($$0, Player.class, 10, false, false, $$12);
            this.enderman = $$0;
            this.isAngerInducing = ($$1, $$2) -> ($$0.isBeingStaredBy((Player)$$1) || $$0.isAngryAt($$1, $$2)) && !$$0.hasIndirectPassenger($$1);
            this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.isAngerInducing);
        }

        @Override
        public boolean canUse() {
            this.pendingTarget = EndermanLookForPlayerGoal.getServerLevel(this.enderman).getNearestPlayer(this.startAggroTargetConditions.range(this.getFollowDistance()), this.enderman);
            return this.pendingTarget != null;
        }

        @Override
        public void start() {
            this.aggroTime = this.adjustedTickDelay(5);
            this.teleportTime = 0;
            this.enderman.setBeingStaredAt();
        }

        @Override
        public void stop() {
            this.pendingTarget = null;
            super.stop();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.pendingTarget != null) {
                if (!this.isAngerInducing.test(this.pendingTarget, EndermanLookForPlayerGoal.getServerLevel(this.enderman))) {
                    return false;
                }
                this.enderman.lookAt(this.pendingTarget, 10.0f, 10.0f);
                return true;
            }
            if (this.target != null) {
                if (this.enderman.hasIndirectPassenger(this.target)) {
                    return false;
                }
                if (this.continueAggroTargetConditions.test(EndermanLookForPlayerGoal.getServerLevel(this.enderman), this.enderman, this.target)) {
                    return true;
                }
            }
            return super.canContinueToUse();
        }

        @Override
        public void tick() {
            if (this.enderman.getTarget() == null) {
                super.setTarget(null);
            }
            if (this.pendingTarget != null) {
                if (--this.aggroTime <= 0) {
                    this.target = this.pendingTarget;
                    this.pendingTarget = null;
                    super.start();
                }
            } else {
                if (this.target != null && !this.enderman.isPassenger()) {
                    if (this.enderman.isBeingStaredBy((Player)this.target)) {
                        if (this.target.distanceToSqr(this.enderman) < 16.0) {
                            this.enderman.teleport();
                        }
                        this.teleportTime = 0;
                    } else if (this.target.distanceToSqr(this.enderman) > 256.0 && this.teleportTime++ >= this.adjustedTickDelay(30) && this.enderman.teleportTowards(this.target)) {
                        this.teleportTime = 0;
                    }
                }
                super.tick();
            }
        }
    }
}

