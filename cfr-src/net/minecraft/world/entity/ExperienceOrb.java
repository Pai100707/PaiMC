/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ExperienceOrb
extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_VALUE = SynchedEntityData.defineId(ExperienceOrb.class, EntityDataSerializers.INT);
    private static final int LIFETIME = 6000;
    private static final int ENTITY_SCAN_PERIOD = 20;
    private static final int MAX_FOLLOW_DIST = 8;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double ORB_MERGE_DISTANCE = 0.5;
    private static final short DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_VALUE = 0;
    private static final int DEFAULT_COUNT = 1;
    private int age = 0;
    private int health = 5;
    private int count = 1;
    private @Nullable Player followingPlayer;
    private final InterpolationHandler interpolation = new InterpolationHandler(this);

    public ExperienceOrb(Level $$0, double $$1, double $$2, double $$3, int $$4) {
        this($$0, new Vec3($$1, $$2, $$3), Vec3.ZERO, $$4);
    }

    public ExperienceOrb(Level $$0, Vec3 $$1, Vec3 $$2, int $$3) {
        this((EntityType<? extends ExperienceOrb>)EntityType.EXPERIENCE_ORB, $$0);
        this.setPos($$1);
        if (!$$0.isClientSide()) {
            this.setYRot(this.random.nextFloat() * 360.0f);
            Vec3 $$4 = new Vec3((this.random.nextDouble() * 0.2 - 0.1) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2 - 0.1) * 2.0);
            if ($$2.lengthSqr() > 0.0 && $$2.dot($$4) < 0.0) {
                $$4 = $$4.scale(-1.0);
            }
            double $$5 = this.getBoundingBox().getSize();
            this.setPos($$1.add($$2.normalize().scale($$5 * 0.5)));
            this.setDeltaMovement($$4);
            if (!$$0.noCollision(this.getBoundingBox())) {
                this.unstuckIfPossible($$5);
            }
        }
        this.setValue($$3);
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> $$0, Level $$1) {
        super($$0, $$1);
    }

    protected void unstuckIfPossible(double $$02) {
        Vec3 $$1 = this.position().add(0.0, (double)this.getBbHeight() / 2.0, 0.0);
        VoxelShape $$2 = Shapes.create(AABB.ofSize($$1, $$02, $$02, $$02));
        this.level().findFreePosition(this, $$2, $$1, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()).ifPresent($$0 -> this.setPos($$0.add(0.0, (double)(-this.getBbHeight()) / 2.0, 0.0)));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_VALUE, 0);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        boolean $$0;
        this.interpolation.interpolate();
        if (this.firstTick && this.level().isClientSide()) {
            this.firstTick = false;
            return;
        }
        super.tick();
        boolean bl = $$0 = !this.level().noCollision(this.getBoundingBox());
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else if (!$$0) {
            this.applyGravity();
        }
        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2f, 0.2f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        if (this.tickCount % 20 == 1) {
            this.scanForMerges();
        }
        this.followNearbyPlayer();
        if (this.followingPlayer == null && !this.level().isClientSide() && $$0) {
            boolean $$1;
            boolean bl2 = $$1 = !this.level().noCollision(this.getBoundingBox().move(this.getDeltaMovement()));
            if ($$1) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
                this.needsSync = true;
            }
        }
        double $$2 = this.getDeltaMovement().y;
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        float $$3 = 0.98f;
        if (this.onGround()) {
            $$3 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
        }
        this.setDeltaMovement(this.getDeltaMovement().scale($$3));
        if (this.verticalCollisionBelow && $$2 < -this.getGravity()) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x, -$$2 * 0.4, this.getDeltaMovement().z));
        }
        ++this.age;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    private void followNearbyPlayer() {
        if (this.followingPlayer == null || this.followingPlayer.isSpectator() || this.followingPlayer.distanceToSqr(this) > 64.0) {
            Player $$0 = this.level().getNearestPlayer(this, 8.0);
            this.followingPlayer = $$0 != null && !$$0.isSpectator() && !$$0.isDeadOrDying() ? $$0 : null;
        }
        if (this.followingPlayer != null) {
            Vec3 $$1 = new Vec3(this.followingPlayer.getX() - this.getX(), this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(), this.followingPlayer.getZ() - this.getZ());
            double $$2 = $$1.lengthSqr();
            double $$3 = 1.0 - Math.sqrt($$2) / 8.0;
            this.setDeltaMovement(this.getDeltaMovement().add($$1.normalize().scale($$3 * $$3 * 0.1)));
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999f);
    }

    private void scanForMerges() {
        if (this.level() instanceof ServerLevel) {
            List<ExperienceOrb> $$0 = this.level().getEntities(EntityTypeTest.forClass(ExperienceOrb.class), this.getBoundingBox().inflate(0.5), this::canMerge);
            for (ExperienceOrb $$1 : $$0) {
                this.merge($$1);
            }
        }
    }

    public static void award(ServerLevel $$0, Vec3 $$1, int $$2) {
        ExperienceOrb.awardWithDirection($$0, $$1, Vec3.ZERO, $$2);
    }

    public static void awardWithDirection(ServerLevel $$0, Vec3 $$1, Vec3 $$2, int $$3) {
        while ($$3 > 0) {
            int $$4 = ExperienceOrb.getExperienceValue($$3);
            $$3 -= $$4;
            if (ExperienceOrb.tryMergeToExisting($$0, $$1, $$4)) continue;
            $$0.addFreshEntity(new ExperienceOrb($$0, $$1, $$2, $$4));
        }
    }

    private static boolean tryMergeToExisting(ServerLevel $$0, Vec3 $$1, int $$22) {
        AABB $$3 = AABB.ofSize($$1, 1.0, 1.0, 1.0);
        int $$4 = $$0.getRandom().nextInt(40);
        List<ExperienceOrb> $$5 = $$0.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), $$3, $$2 -> ExperienceOrb.canMerge($$2, $$4, $$22));
        if (!$$5.isEmpty()) {
            ExperienceOrb $$6 = $$5.get(0);
            ++$$6.count;
            $$6.age = 0;
            return true;
        }
        return false;
    }

    private boolean canMerge(ExperienceOrb $$0) {
        return $$0 != this && ExperienceOrb.canMerge($$0, this.getId(), this.getValue());
    }

    private static boolean canMerge(ExperienceOrb $$0, int $$1, int $$2) {
        return !$$0.isRemoved() && ($$0.getId() - $$1) % 40 == 0 && $$0.getValue() == $$2;
    }

    private void merge(ExperienceOrb $$0) {
        this.count += $$0.count;
        this.age = Math.min(this.age, $$0.age);
        $$0.discard();
    }

    private void setUnderwaterMovement() {
        Vec3 $$0 = this.getDeltaMovement();
        this.setDeltaMovement($$0.x * (double)0.99f, Math.min($$0.y + (double)5.0E-4f, (double)0.06f), $$0.z * (double)0.99f);
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public final boolean hurtClient(DamageSource $$0) {
        return !this.isInvulnerableToBase($$0);
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        this.markHurt();
        this.health = (int)((float)this.health - $$2);
        if (this.health <= 0) {
            this.discard();
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.putShort("Health", (short)this.health);
        $$0.putShort("Age", (short)this.age);
        $$0.putShort("Value", (short)this.getValue());
        $$0.putInt("Count", this.count);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.health = $$0.getShortOr("Health", (short)5);
        this.age = $$0.getShortOr("Age", (short)0);
        this.setValue($$0.getShortOr("Value", (short)0));
        this.count = $$0.read("Count", ExtraCodecs.POSITIVE_INT).orElse(1);
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void playerTouch(Player $$0) {
        if (!($$0 instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer $$1 = (ServerPlayer)$$0;
        if ($$0.takeXpDelay == 0) {
            void $$2;
            $$0.takeXpDelay = 2;
            $$0.take(this, 1);
            int $$3 = this.repairPlayerItems((ServerPlayer)$$2, this.getValue());
            if ($$3 > 0) {
                $$0.giveExperiencePoints($$3);
            }
            --this.count;
            if (this.count == 0) {
                this.discard();
            }
        }
    }

    private int repairPlayerItems(ServerPlayer $$0, int $$1) {
        Optional<EnchantedItemInUse> $$2 = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, $$0, ItemStack::isDamaged);
        if ($$2.isPresent()) {
            int $$6;
            ItemStack $$3 = $$2.get().itemStack();
            int $$4 = EnchantmentHelper.modifyDurabilityToRepairFromXp($$0.level(), $$3, $$1);
            int $$5 = Math.min($$4, $$3.getDamageValue());
            $$3.setDamageValue($$3.getDamageValue() - $$5);
            if ($$5 > 0 && ($$6 = $$1 - $$5 * $$1 / $$4) > 0) {
                return this.repairPlayerItems($$0, $$6);
            }
            return 0;
        }
        return $$1;
    }

    public int getValue() {
        return this.entityData.get(DATA_VALUE);
    }

    private void setValue(int $$0) {
        this.entityData.set(DATA_VALUE, $$0);
    }

    public int getIcon() {
        int $$0 = this.getValue();
        if ($$0 >= 2477) {
            return 10;
        }
        if ($$0 >= 1237) {
            return 9;
        }
        if ($$0 >= 617) {
            return 8;
        }
        if ($$0 >= 307) {
            return 7;
        }
        if ($$0 >= 149) {
            return 6;
        }
        if ($$0 >= 73) {
            return 5;
        }
        if ($$0 >= 37) {
            return 4;
        }
        if ($$0 >= 17) {
            return 3;
        }
        if ($$0 >= 7) {
            return 2;
        }
        if ($$0 >= 3) {
            return 1;
        }
        return 0;
    }

    public static int getExperienceValue(int $$0) {
        if ($$0 >= 2477) {
            return 2477;
        }
        if ($$0 >= 1237) {
            return 1237;
        }
        if ($$0 >= 617) {
            return 617;
        }
        if ($$0 >= 307) {
            return 307;
        }
        if ($$0 >= 149) {
            return 149;
        }
        if ($$0 >= 73) {
            return 73;
        }
        if ($$0 >= 37) {
            return 37;
        }
        if ($$0 >= 17) {
            return 17;
        }
        if ($$0 >= 7) {
            return 7;
        }
        if ($$0 >= 3) {
            return 3;
        }
        return 1;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }
}

