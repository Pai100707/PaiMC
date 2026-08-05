/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.rabbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Rabbit
extends Animal {
    public static final double STROLL_SPEED_MOD = 0.6;
    public static final double BREED_SPEED_MOD = 0.8;
    public static final double FOLLOW_SPEED_MOD = 1.0;
    public static final double FLEE_SPEED_MOD = 2.2;
    public static final double ATTACK_SPEED_MOD = 1.4;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final int DEFAULT_MORE_CARROT_TICKS = 0;
    private static final Identifier KILLER_BUNNY = Identifier.withDefaultNamespace("killer_bunny");
    private static final int DEFAULT_ATTACK_POWER = 3;
    private static final int EVIL_ATTACK_POWER_INCREMENT = 5;
    private static final Identifier EVIL_ATTACK_POWER_MODIFIER = Identifier.withDefaultNamespace("evil");
    private static final int EVIL_ARMOR_VALUE = 8;
    private static final int MORE_CARROTS_DELAY = 40;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    int moreCarrotTicks = 0;

    public Rabbit(EntityType<? extends Rabbit> $$0, Level $$1) {
        super((EntityType<? extends Animal>)$$0, $$1);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
        this.goalSelector.addGoal(1, new RabbitPanicGoal(this, 2.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, $$0 -> $$0.is(ItemTags.RABBIT_FOOD), false));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Player>(this, Player.class, 8.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Wolf>(this, Wolf.class, 10.0f, 2.2, 2.2));
        this.goalSelector.addGoal(4, new RabbitAvoidEntityGoal<Monster>(this, Monster.class, 4.0f, 2.2, 2.2));
        this.goalSelector.addGoal(5, new RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0f));
    }

    @Override
    protected float getJumpPower() {
        Path $$1;
        float $$0 = 0.3f;
        if (this.moveControl.getSpeedModifier() <= 0.6) {
            $$0 = 0.2f;
        }
        if (($$1 = this.navigation.getPath()) != null && !$$1.isDone()) {
            Vec3 $$2 = $$1.getNextEntityPos(this);
            if ($$2.y > this.getY() + 0.5) {
                $$0 = 0.5f;
            }
        }
        if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + 0.5) {
            $$0 = 0.5f;
        }
        return super.getJumpPower($$0 / 0.42f);
    }

    @Override
    public void jumpFromGround() {
        double $$1;
        super.jumpFromGround();
        double $$0 = this.moveControl.getSpeedModifier();
        if ($$0 > 0.0 && ($$1 = this.getDeltaMovement().horizontalDistanceSqr()) < 0.01) {
            this.moveRelative(0.1f, new Vec3(0.0, 0.0, 1.0));
        }
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte)1);
        }
    }

    public float getJumpCompletion(float $$0) {
        if (this.jumpDuration == 0) {
            return 0.0f;
        }
        return ((float)this.jumpTicks + $$0) / (float)this.jumpDuration;
    }

    public void setSpeedModifier(double $$0) {
        this.getNavigation().setSpeedModifier($$0);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), $$0);
    }

    @Override
    public void setJumping(boolean $$0) {
        super.setJumping($$0);
        if ($$0) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
        }
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_TYPE_ID, Variant.DEFAULT.id);
    }

    @Override
    public void customServerAiStep(ServerLevel $$0) {
        if (this.jumpDelayTicks > 0) {
            --this.jumpDelayTicks;
        }
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
        if (this.onGround()) {
            RabbitJumpControl $$2;
            LivingEntity $$1;
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }
            if (this.getVariant() == Variant.EVIL && this.jumpDelayTicks == 0 && ($$1 = this.getTarget()) != null && this.distanceToSqr($$1) < 16.0) {
                this.facePoint($$1.getX(), $$1.getZ());
                this.moveControl.setWantedPosition($$1.getX(), $$1.getY(), $$1.getZ(), this.moveControl.getSpeedModifier());
                this.startJumping();
                this.wasOnGround = true;
            }
            if (!($$2 = (RabbitJumpControl)this.jumpControl).wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path $$3 = this.navigation.getPath();
                    Vec3 $$4 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if ($$3 != null && !$$3.isDone()) {
                        $$4 = $$3.getNextEntityPos(this);
                    }
                    this.facePoint($$4.x, $$4.z);
                    this.startJumping();
                }
            } else if (!$$2.canJump()) {
                this.enableJumpControl();
            }
        }
        this.wasOnGround = this.onGround();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double $$0, double $$1) {
        this.setYRot((float)(Mth.atan2($$1 - this.getZ(), $$0 - this.getX()) * 57.2957763671875) - 90.0f);
    }

    private void enableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(false);
    }

    private void setLandingDelay() {
        this.jumpDelayTicks = this.moveControl.getSpeedModifier() < 2.2 ? 10 : 1;
    }

    private void checkLandingDelay() {
        this.setLandingDelay();
        this.disableJumpControl();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("RabbitType", Variant.LEGACY_CODEC, this.getVariant());
        $$0.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setVariant($$0.read("RabbitType", Variant.LEGACY_CODEC).orElse(Variant.DEFAULT));
        this.moreCarrotTicks = $$0.getIntOr("MoreCarrotTicks", 0);
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.RABBIT_JUMP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    public void playAttackSound() {
        if (this.getVariant() == Variant.EVIL) {
            this.playSound(SoundEvents.RABBIT_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    @Override
    public SoundSource getSoundSource() {
        return this.getVariant() == Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public @Nullable Rabbit getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        block2: {
            block3: {
                $$2 = EntityType.RABBIT.create($$0, EntitySpawnReason.BREEDING);
                if ($$2 == null) break block2;
                $$3 = Rabbit.getRandomRabbitVariant($$0, this.blockPosition());
                if (this.random.nextInt(20) == 0) break block3;
                if (!($$1 instanceof Rabbit)) ** GOTO lbl-1000
                $$4 = (Rabbit)$$1;
                if (this.random.nextBoolean()) {
                    $$3 = $$4.getVariant();
                } else lbl-1000:
                // 2 sources

                {
                    $$3 = this.getVariant();
                }
            }
            $$2.setVariant($$3);
        }
        return $$2;
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.RABBIT_FOOD);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    private void setVariant(Variant $$0) {
        if ($$0 == Variant.EVIL) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.4, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Wolf>((Mob)this, Wolf.class, true));
            this.getAttribute(Attributes.ATTACK_DAMAGE).addOrUpdateTransientModifier(new AttributeModifier(EVIL_ATTACK_POWER_MODIFIER, 5.0, AttributeModifier.Operation.ADD_VALUE));
            if (!this.hasCustomName()) {
                this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
            }
        } else {
            this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(EVIL_ATTACK_POWER_MODIFIER);
        }
        this.entityData.set(DATA_TYPE_ID, $$0.id);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.RABBIT_VARIANT) {
            return Rabbit.castComponentValue($$0, this.getVariant());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.RABBIT_VARIANT);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.RABBIT_VARIANT) {
            this.setVariant(Rabbit.castComponentValue(DataComponents.RABBIT_VARIANT, $$1));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        Variant $$4 = Rabbit.getRandomRabbitVariant($$0, this.blockPosition());
        if ($$3 instanceof RabbitGroupData) {
            $$4 = ((RabbitGroupData)$$3).variant;
        } else {
            $$3 = new RabbitGroupData($$4);
        }
        this.setVariant($$4);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    private static Variant getRandomRabbitVariant(LevelAccessor $$0, BlockPos $$1) {
        Holder<Biome> $$2 = $$0.getBiome($$1);
        int $$3 = $$0.getRandom().nextInt(100);
        if ($$2.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
            return $$3 < 80 ? Variant.WHITE : Variant.WHITE_SPLOTCHED;
        }
        if ($$2.is(BiomeTags.SPAWNS_GOLD_RABBITS)) {
            return Variant.GOLD;
        }
        return $$3 < 50 ? Variant.BROWN : ($$3 < 90 ? Variant.SALT : Variant.BLACK);
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getBlockState($$3.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && Rabbit.isBrightEnoughToSpawn($$1, $$3);
    }

    boolean wantsMoreFood() {
        return this.moreCarrotTicks <= 0;
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 1) {
            this.spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static class RabbitJumpControl
    extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit $$0) {
            super($$0);
            this.rabbit = $$0;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean $$0) {
            this.canJump = $$0;
        }

        @Override
        public void tick() {
            if (this.jump) {
                this.rabbit.startJumping();
                this.jump = false;
            }
        }
    }

    static class RabbitMoveControl
    extends MoveControl {
        private final Rabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveControl(Rabbit $$0) {
            super($$0);
            this.rabbit = $$0;
        }

        @Override
        public void tick() {
            if (this.rabbit.onGround() && !this.rabbit.jumping && !((RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
                this.rabbit.setSpeedModifier(0.0);
            } else if (this.hasWanted() || this.operation == MoveControl.Operation.JUMPING) {
                this.rabbit.setSpeedModifier(this.nextJumpSpeed);
            }
            super.tick();
        }

        @Override
        public void setWantedPosition(double $$0, double $$1, double $$2, double $$3) {
            if (this.rabbit.isInWater()) {
                $$3 = 1.5;
            }
            super.setWantedPosition($$0, $$1, $$2, $$3);
            if ($$3 > 0.0) {
                this.nextJumpSpeed = $$3;
            }
        }
    }

    static class RabbitPanicGoal
    extends PanicGoal {
        private final Rabbit rabbit;

        public RabbitPanicGoal(Rabbit $$0, double $$1) {
            super($$0, $$1);
            this.rabbit = $$0;
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.setSpeedModifier(this.speedModifier);
        }
    }

    static class RabbitAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Rabbit rabbit;

        public RabbitAvoidEntityGoal(Rabbit $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
            super($$0, $$1, $$2, $$3, $$4);
            this.rabbit = $$0;
        }

        @Override
        public boolean canUse() {
            return this.rabbit.getVariant() != Variant.EVIL && super.canUse();
        }
    }

    static class RaidGardenGoal
    extends MoveToBlockGoal {
        private final Rabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public RaidGardenGoal(Rabbit $$0) {
            super($$0, 0.7f, 16);
            this.rabbit = $$0;
        }

        @Override
        public boolean canUse() {
            if (this.nextStartTick <= 0) {
                if (!RaidGardenGoal.getServerLevel(this.rabbit).getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                    return false;
                }
                this.canRaid = false;
                this.wantsToRaid = this.rabbit.wantsMoreFood();
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canRaid && super.canContinueToUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.getLookControl().setLookAt((double)this.blockPos.getX() + 0.5, this.blockPos.getY() + 1, (double)this.blockPos.getZ() + 0.5, 10.0f, this.rabbit.getMaxHeadXRot());
            if (this.isReachedTarget()) {
                Level $$0 = this.rabbit.level();
                BlockPos $$1 = this.blockPos.above();
                BlockState $$2 = $$0.getBlockState($$1);
                Block $$3 = $$2.getBlock();
                if (this.canRaid && $$3 instanceof CarrotBlock) {
                    int $$4 = $$2.getValue(CarrotBlock.AGE);
                    if ($$4 == 0) {
                        $$0.setBlock($$1, Blocks.AIR.defaultBlockState(), 2);
                        $$0.destroyBlock($$1, true, this.rabbit);
                    } else {
                        $$0.setBlock($$1, (BlockState)$$2.setValue(CarrotBlock.AGE, $$4 - 1), 2);
                        $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$1, GameEvent.Context.of(this.rabbit));
                        $$0.levelEvent(2001, $$1, Block.getId($$2));
                    }
                    this.rabbit.moreCarrotTicks = 40;
                }
                this.canRaid = false;
                this.nextStartTick = 10;
            }
        }

        @Override
        protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
            BlockState $$2 = $$0.getBlockState($$1);
            if ($$2.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid && ($$2 = $$0.getBlockState($$1.above())).getBlock() instanceof CarrotBlock && ((CarrotBlock)$$2.getBlock()).isMaxAge($$2)) {
                this.canRaid = true;
                return true;
            }
            return false;
        }
    }

    public static enum Variant implements StringRepresentable
    {
        BROWN(0, "brown"),
        WHITE(1, "white"),
        BLACK(2, "black"),
        WHITE_SPLOTCHED(3, "white_splotched"),
        GOLD(4, "gold"),
        SALT(5, "salt"),
        EVIL(99, "evil");

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

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int id() {
            return this.id;
        }

        public static Variant byId(int $$0) {
            return BY_ID.apply($$0);
        }

        static {
            DEFAULT = BROWN;
            BY_ID = ByIdMap.sparse(Variant::id, Variant.values(), DEFAULT);
            CODEC = StringRepresentable.fromEnum(Variant::values);
            LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Variant::id);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::id);
        }
    }

    public static class RabbitGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public RabbitGroupData(Variant $$0) {
            super(1.0f);
            this.variant = $$0;
        }
    }
}

