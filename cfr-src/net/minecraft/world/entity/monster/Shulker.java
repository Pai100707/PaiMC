/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class Shulker
extends AbstractGolem
implements Enemy {
    private static final Identifier COVERED_ARMOR_MODIFIER_ID = Identifier.withDefaultNamespace("covered");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_ID, 20.0, AttributeModifier.Operation.ADD_VALUE);
    protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
    protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    private static final int TELEPORT_STEPS = 6;
    private static final byte NO_COLOR = 16;
    private static final byte DEFAULT_COLOR = 16;
    private static final int MAX_TELEPORT_DISTANCE = 8;
    private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
    private static final int OTHER_SHULKER_LIMIT = 5;
    private static final float PEEK_PER_TICK = 0.05f;
    private static final byte DEFAULT_PEEK = 0;
    private static final Direction DEFAULT_ATTACH_FACE = Direction.DOWN;
    static final Vector3f FORWARD = Util.make(() -> {
        Vec3i $$0 = Direction.SOUTH.getUnitVec3i();
        return new Vector3f((float)$$0.getX(), (float)$$0.getY(), (float)$$0.getZ());
    });
    private static final float MAX_SCALE = 3.0f;
    private float currentPeekAmountO;
    private float currentPeekAmount;
    private @Nullable BlockPos clientOldAttachPosition;
    private int clientSideTeleportInterpolation;
    private static final float MAX_LID_OPEN = 1.0f;

    public Shulker(EntityType<? extends Shulker> $$0, Level $$1) {
        super((EntityType<? extends AbstractGolem>)$$0, $$1);
        this.xpReward = 5;
        this.lookControl = new ShulkerLookControl(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f, 0.02f, true));
        this.goalSelector.addGoal(4, new ShulkerAttackGoal());
        this.goalSelector.addGoal(7, new ShulkerPeekGoal());
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new ShulkerNearestAttackGoal(this));
        this.targetSelector.addGoal(3, new ShulkerDefenseAttackGoal(this));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHULKER_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHULKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isClosed()) {
            return SoundEvents.SHULKER_HURT_CLOSED;
        }
        return SoundEvents.SHULKER_HURT;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ATTACH_FACE_ID, DEFAULT_ATTACH_FACE);
        $$0.define(DATA_PEEK_ID, (byte)0);
        $$0.define(DATA_COLOR_ID, (byte)16);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new ShulkerBodyRotationControl(this);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setAttachFace($$0.read("AttachFace", Direction.LEGACY_ID_CODEC).orElse(DEFAULT_ATTACH_FACE));
        this.entityData.set(DATA_PEEK_ID, $$0.getByteOr("Peek", (byte)0));
        this.entityData.set(DATA_COLOR_ID, $$0.getByteOr("Color", (byte)16));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.store("AttachFace", Direction.LEGACY_ID_CODEC, this.getAttachFace());
        $$0.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
        $$0.putByte("Color", this.entityData.get(DATA_COLOR_ID));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level().isClientSide() || this.isPassenger() || this.canStayAt(this.blockPosition(), this.getAttachFace()))) {
            this.findNewAttachment();
        }
        if (this.updatePeekAmount()) {
            this.onPeekAmountChange();
        }
        if (this.level().isClientSide()) {
            if (this.clientSideTeleportInterpolation > 0) {
                --this.clientSideTeleportInterpolation;
            } else {
                this.clientOldAttachPosition = null;
            }
        }
    }

    private void findNewAttachment() {
        Direction $$0 = this.findAttachableSurface(this.blockPosition());
        if ($$0 != null) {
            this.setAttachFace($$0);
        } else {
            this.teleportSomewhere();
        }
    }

    @Override
    protected AABB makeBoundingBox(Vec3 $$0) {
        float $$1 = Shulker.getPhysicalPeek(this.currentPeekAmount);
        Direction $$2 = this.getAttachFace().getOpposite();
        return Shulker.getProgressAabb(this.getScale(), $$2, $$1, $$0);
    }

    private static float getPhysicalPeek(float $$0) {
        return 0.5f - Mth.sin((0.5f + $$0) * (float)Math.PI) * 0.5f;
    }

    private boolean updatePeekAmount() {
        this.currentPeekAmountO = this.currentPeekAmount;
        float $$0 = (float)this.getRawPeekAmount() * 0.01f;
        if (this.currentPeekAmount == $$0) {
            return false;
        }
        this.currentPeekAmount = this.currentPeekAmount > $$0 ? Mth.clamp(this.currentPeekAmount - 0.05f, $$0, 1.0f) : Mth.clamp(this.currentPeekAmount + 0.05f, 0.0f, $$0);
        return true;
    }

    private void onPeekAmountChange() {
        this.reapplyPosition();
        float $$02 = Shulker.getPhysicalPeek(this.currentPeekAmount);
        float $$1 = Shulker.getPhysicalPeek(this.currentPeekAmountO);
        Direction $$2 = this.getAttachFace().getOpposite();
        float $$3 = ($$02 - $$1) * this.getScale();
        if ($$3 <= 0.0f) {
            return;
        }
        List<Entity> $$4 = this.level().getEntities(this, Shulker.getProgressDeltaAabb(this.getScale(), $$2, $$1, $$02, this.position()), EntitySelector.NO_SPECTATORS.and($$0 -> !$$0.isPassengerOfSameVehicle(this)));
        for (Entity $$5 : $$4) {
            if ($$5 instanceof Shulker || $$5.noPhysics) continue;
            $$5.move(MoverType.SHULKER, new Vec3($$3 * (float)$$2.getStepX(), $$3 * (float)$$2.getStepY(), $$3 * (float)$$2.getStepZ()));
        }
    }

    public static AABB getProgressAabb(float $$0, Direction $$1, float $$2, Vec3 $$3) {
        return Shulker.getProgressDeltaAabb($$0, $$1, -1.0f, $$2, $$3);
    }

    public static AABB getProgressDeltaAabb(float $$0, Direction $$1, float $$2, float $$3, Vec3 $$4) {
        AABB $$5 = new AABB((double)(-$$0) * 0.5, 0.0, (double)(-$$0) * 0.5, (double)$$0 * 0.5, $$0, (double)$$0 * 0.5);
        double $$6 = Math.max($$2, $$3);
        double $$7 = Math.min($$2, $$3);
        AABB $$8 = $$5.expandTowards((double)$$1.getStepX() * $$6 * (double)$$0, (double)$$1.getStepY() * $$6 * (double)$$0, (double)$$1.getStepZ() * $$6 * (double)$$0).contract((double)(-$$1.getStepX()) * (1.0 + $$7) * (double)$$0, (double)(-$$1.getStepY()) * (1.0 + $$7) * (double)$$0, (double)(-$$1.getStepZ()) * (1.0 + $$7) * (double)$$0);
        return $$8.move($$4.x, $$4.y, $$4.z);
    }

    @Override
    public boolean startRiding(Entity $$0, boolean $$1, boolean $$2) {
        if (this.level().isClientSide()) {
            this.clientOldAttachPosition = null;
            this.clientSideTeleportInterpolation = 0;
        }
        this.setAttachFace(Direction.DOWN);
        return super.startRiding($$0, $$1, $$2);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (this.level().isClientSide()) {
            this.clientOldAttachPosition = this.blockPosition();
        }
        this.yBodyRotO = 0.0f;
        this.yBodyRot = 0.0f;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        this.setYRot(0.0f);
        this.yHeadRot = this.getYRot();
        this.setOldPosAndRot();
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public void move(MoverType $$0, Vec3 $$1) {
        if ($$0 == MoverType.SHULKER_BOX) {
            this.teleportSomewhere();
        } else {
            super.move($$0, $$1);
        }
    }

    @Override
    public Vec3 getDeltaMovement() {
        return Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(Vec3 $$0) {
    }

    @Override
    public void setPos(double $$0, double $$1, double $$2) {
        BlockPos $$3 = this.blockPosition();
        if (this.isPassenger()) {
            super.setPos($$0, $$1, $$2);
        } else {
            super.setPos((double)Mth.floor($$0) + 0.5, Mth.floor($$1 + 0.5), (double)Mth.floor($$2) + 0.5);
        }
        if (this.tickCount == 0) {
            return;
        }
        BlockPos $$4 = this.blockPosition();
        if (!$$4.equals($$3)) {
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.needsSync = true;
            if (this.level().isClientSide() && !this.isPassenger() && !$$4.equals(this.clientOldAttachPosition)) {
                this.clientOldAttachPosition = $$3;
                this.clientSideTeleportInterpolation = 6;
                this.xOld = this.getX();
                this.yOld = this.getY();
                this.zOld = this.getZ();
            }
        }
    }

    protected @Nullable Direction findAttachableSurface(BlockPos $$0) {
        for (Direction $$1 : Direction.values()) {
            if (!this.canStayAt($$0, $$1)) continue;
            return $$1;
        }
        return null;
    }

    boolean canStayAt(BlockPos $$0, Direction $$1) {
        if (this.isPositionBlocked($$0)) {
            return false;
        }
        Direction $$2 = $$1.getOpposite();
        if (!this.level().loadedAndEntityCanStandOnFace($$0.relative($$1), this, $$2)) {
            return false;
        }
        AABB $$3 = Shulker.getProgressAabb(this.getScale(), $$2, 1.0f, $$0.getBottomCenter()).deflate(1.0E-6);
        return this.level().noCollision(this, $$3);
    }

    private boolean isPositionBlocked(BlockPos $$0) {
        BlockState $$1 = this.level().getBlockState($$0);
        if ($$1.isAir()) {
            return false;
        }
        boolean $$2 = $$1.is(Blocks.MOVING_PISTON) && $$0.equals(this.blockPosition());
        return !$$2;
    }

    protected boolean teleportSomewhere() {
        if (this.isNoAi() || !this.isAlive()) {
            return false;
        }
        BlockPos $$0 = this.blockPosition();
        for (int $$1 = 0; $$1 < 5; ++$$1) {
            Direction $$3;
            BlockPos $$2 = $$0.offset(Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8));
            if ($$2.getY() <= this.level().getMinY() || !this.level().isEmptyBlock($$2) || !this.level().getWorldBorder().isWithinBounds($$2) || !this.level().noCollision(this, new AABB($$2).deflate(1.0E-6)) || ($$3 = this.findAttachableSurface($$2)) == null) continue;
            this.unRide();
            this.setAttachFace($$3);
            this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0f, 1.0f);
            this.setPos((double)$$2.getX() + 0.5, $$2.getY(), (double)$$2.getZ() + 0.5);
            this.level().gameEvent(GameEvent.TELEPORT, $$0, GameEvent.Context.of(this));
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.setTarget(null);
            return true;
        }
        return false;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return null;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity $$3;
        if (this.isClosed() && ($$3 = $$1.getDirectEntity()) instanceof AbstractArrow) {
            return false;
        }
        if (super.hurtServer($$0, $$1, $$2)) {
            Entity $$4;
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.teleportSomewhere();
            } else if ($$1.is(DamageTypeTags.IS_PROJECTILE) && ($$4 = $$1.getDirectEntity()) != null && $$4.getType() == EntityType.SHULKER_BULLET) {
                this.hitByShulkerBullet();
            }
            return true;
        }
        return false;
    }

    private boolean isClosed() {
        return this.getRawPeekAmount() == 0;
    }

    private void hitByShulkerBullet() {
        Vec3 $$0 = this.position();
        AABB $$1 = this.getBoundingBox();
        if (this.isClosed() || !this.teleportSomewhere()) {
            return;
        }
        int $$2 = this.level().getEntities(EntityType.SHULKER, $$1.inflate(8.0), Entity::isAlive).size();
        float $$3 = (float)($$2 - 1) / 5.0f;
        if (this.level().random.nextFloat() < $$3) {
            return;
        }
        Shulker $$4 = EntityType.SHULKER.create(this.level(), EntitySpawnReason.BREEDING);
        if ($$4 != null) {
            $$4.setVariant(this.getVariant());
            $$4.snapTo($$0);
            this.level().addFreshEntity($$4);
        }
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity $$0) {
        return this.isAlive();
    }

    public Direction getAttachFace() {
        return this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    private void setAttachFace(Direction $$0) {
        this.entityData.set(DATA_ATTACH_FACE_ID, $$0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_ATTACH_FACE_ID.equals($$0)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
        super.onSyncedDataUpdated($$0);
    }

    private int getRawPeekAmount() {
        return this.entityData.get(DATA_PEEK_ID).byteValue();
    }

    void setRawPeekAmount(int $$0) {
        if (!this.level().isClientSide()) {
            this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
            if ($$0 == 0) {
                this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                this.playSound(SoundEvents.SHULKER_CLOSE, 1.0f, 1.0f);
                this.gameEvent(GameEvent.CONTAINER_CLOSE);
            } else {
                this.playSound(SoundEvents.SHULKER_OPEN, 1.0f, 1.0f);
                this.gameEvent(GameEvent.CONTAINER_OPEN);
            }
        }
        this.entityData.set(DATA_PEEK_ID, (byte)$$0);
    }

    public float getClientPeekAmount(float $$0) {
        return Mth.lerp($$0, this.currentPeekAmountO, this.currentPeekAmount);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        this.yBodyRot = 0.0f;
        this.yBodyRotO = 0.0f;
    }

    @Override
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override
    public void push(Entity $$0) {
    }

    public @Nullable Vec3 getRenderPosition(float $$0) {
        if (this.clientOldAttachPosition == null || this.clientSideTeleportInterpolation <= 0) {
            return null;
        }
        double $$1 = (double)((float)this.clientSideTeleportInterpolation - $$0) / 6.0;
        $$1 *= $$1;
        BlockPos $$2 = this.blockPosition();
        double $$3 = (double)($$2.getX() - this.clientOldAttachPosition.getX()) * ($$1 *= (double)this.getScale());
        double $$4 = (double)($$2.getY() - this.clientOldAttachPosition.getY()) * $$1;
        double $$5 = (double)($$2.getZ() - this.clientOldAttachPosition.getZ()) * $$1;
        return new Vec3(-$$3, -$$4, -$$5);
    }

    @Override
    protected float sanitizeScale(float $$0) {
        return Math.min($$0, 3.0f);
    }

    private void setVariant(Optional<DyeColor> $$02) {
        this.entityData.set(DATA_COLOR_ID, $$02.map($$0 -> (byte)$$0.getId()).orElse((byte)16));
    }

    public Optional<DyeColor> getVariant() {
        return Optional.ofNullable(this.getColor());
    }

    public @Nullable DyeColor getColor() {
        byte $$0 = this.entityData.get(DATA_COLOR_ID);
        if ($$0 == 16 || $$0 > 15) {
            return null;
        }
        return DyeColor.byId($$0);
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        if ($$0 == DataComponents.SHULKER_COLOR) {
            return Shulker.castComponentValue($$0, this.getColor());
        }
        return super.get($$0);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter $$0) {
        this.applyImplicitComponentIfPresent($$0, DataComponents.SHULKER_COLOR);
        super.applyImplicitComponents($$0);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
        if ($$0 == DataComponents.SHULKER_COLOR) {
            this.setVariant(Optional.of(Shulker.castComponentValue(DataComponents.SHULKER_COLOR, $$1)));
            return true;
        }
        return super.applyImplicitComponent($$0, $$1);
    }

    class ShulkerLookControl
    extends LookControl {
        public ShulkerLookControl(Mob $$0) {
            super($$0);
        }

        @Override
        protected void clampHeadRotationToBody() {
        }

        @Override
        protected Optional<Float> getYRotD() {
            Direction $$0 = Shulker.this.getAttachFace().getOpposite();
            Vector3f $$1 = $$0.getRotation().transform(new Vector3f((Vector3fc)FORWARD));
            Vec3i $$2 = $$0.getUnitVec3i();
            Vector3f $$3 = new Vector3f((float)$$2.getX(), (float)$$2.getY(), (float)$$2.getZ());
            $$3.cross((Vector3fc)$$1);
            double $$4 = this.wantedX - this.mob.getX();
            double $$5 = this.wantedY - this.mob.getEyeY();
            double $$6 = this.wantedZ - this.mob.getZ();
            Vector3f $$7 = new Vector3f((float)$$4, (float)$$5, (float)$$6);
            float $$8 = $$3.dot((Vector3fc)$$7);
            float $$9 = $$1.dot((Vector3fc)$$7);
            return Math.abs($$8) > 1.0E-5f || Math.abs($$9) > 1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2(-$$8, $$9) * 57.2957763671875))) : Optional.empty();
        }

        @Override
        protected Optional<Float> getXRotD() {
            return Optional.of(Float.valueOf(0.0f));
        }
    }

    class ShulkerAttackGoal
    extends Goal {
        private int attackTime;

        public ShulkerAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity $$0 = Shulker.this.getTarget();
            if ($$0 == null || !$$0.isAlive()) {
                return false;
            }
            return Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void start() {
            this.attackTime = 20;
            Shulker.this.setRawPeekAmount(100);
        }

        @Override
        public void stop() {
            Shulker.this.setRawPeekAmount(0);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            --this.attackTime;
            LivingEntity $$0 = Shulker.this.getTarget();
            if ($$0 == null) {
                return;
            }
            Shulker.this.getLookControl().setLookAt($$0, 180.0f, 180.0f);
            double $$1 = Shulker.this.distanceToSqr($$0);
            if ($$1 < 400.0) {
                if (this.attackTime <= 0) {
                    this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
                    Shulker.this.level().addFreshEntity(new ShulkerBullet(Shulker.this.level(), Shulker.this, $$0, Shulker.this.getAttachFace().getAxis()));
                    Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0f, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2f + 1.0f);
                }
            } else {
                Shulker.this.setTarget(null);
            }
            super.tick();
        }
    }

    class ShulkerPeekGoal
    extends Goal {
        private int peekTime;

        ShulkerPeekGoal() {
        }

        @Override
        public boolean canUse() {
            return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(ShulkerPeekGoal.reducedTickDelay(40)) == 0 && Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
        }

        @Override
        public boolean canContinueToUse() {
            return Shulker.this.getTarget() == null && this.peekTime > 0;
        }

        @Override
        public void start() {
            this.peekTime = this.adjustedTickDelay(20 * (1 + Shulker.this.random.nextInt(3)));
            Shulker.this.setRawPeekAmount(30);
        }

        @Override
        public void stop() {
            if (Shulker.this.getTarget() == null) {
                Shulker.this.setRawPeekAmount(0);
            }
        }

        @Override
        public void tick() {
            --this.peekTime;
        }
    }

    class ShulkerNearestAttackGoal
    extends NearestAttackableTargetGoal<Player> {
        public ShulkerNearestAttackGoal(Shulker $$0) {
            super((Mob)$$0, Player.class, true);
        }

        @Override
        public boolean canUse() {
            if (Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double $$0) {
            Direction $$1 = ((Shulker)this.mob).getAttachFace();
            if ($$1.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, $$0, $$0);
            }
            if ($$1.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate($$0, $$0, 4.0);
            }
            return this.mob.getBoundingBox().inflate($$0, 4.0, $$0);
        }
    }

    static class ShulkerDefenseAttackGoal
    extends NearestAttackableTargetGoal<LivingEntity> {
        public ShulkerDefenseAttackGoal(Shulker $$02) {
            super($$02, LivingEntity.class, 10, true, false, ($$0, $$1) -> $$0 instanceof Enemy);
        }

        @Override
        public boolean canUse() {
            if (this.mob.getTeam() == null) {
                return false;
            }
            return super.canUse();
        }

        @Override
        protected AABB getTargetSearchArea(double $$0) {
            Direction $$1 = ((Shulker)this.mob).getAttachFace();
            if ($$1.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0, $$0, $$0);
            }
            if ($$1.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate($$0, $$0, 4.0);
            }
            return this.mob.getBoundingBox().inflate($$0, 4.0, $$0);
        }
    }

    static class ShulkerBodyRotationControl
    extends BodyRotationControl {
        public ShulkerBodyRotationControl(Mob $$0) {
            super($$0);
        }

        @Override
        public void clientTick() {
        }
    }
}

