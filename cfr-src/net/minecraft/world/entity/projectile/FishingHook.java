/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.projectile;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FishingHook
extends Projectile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomSource syncronizedRandom = RandomSource.create();
    private boolean biting;
    private int outOfWaterTime;
    private static final int MAX_OUT_OF_WATER_TIME = 10;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater = true;
    private @Nullable Entity hookedIn;
    private FishHookState currentState = FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;
    private final InterpolationHandler interpolationHandler = new InterpolationHandler(this);

    private FishingHook(EntityType<? extends FishingHook> $$0, Level $$1, int $$2, int $$3) {
        super((EntityType<? extends Projectile>)$$0, $$1);
        this.luck = Math.max(0, $$2);
        this.lureSpeed = Math.max(0, $$3);
    }

    public FishingHook(EntityType<? extends FishingHook> $$0, Level $$1) {
        this($$0, $$1, 0, 0);
    }

    public FishingHook(Player $$0, Level $$1, int $$2, int $$3) {
        this(EntityType.FISHING_BOBBER, $$1, $$2, $$3);
        this.setOwner($$0);
        float $$4 = $$0.getXRot();
        float $$5 = $$0.getYRot();
        float $$6 = Mth.cos(-$$5 * ((float)Math.PI / 180) - (float)Math.PI);
        float $$7 = Mth.sin(-$$5 * ((float)Math.PI / 180) - (float)Math.PI);
        float $$8 = -Mth.cos(-$$4 * ((float)Math.PI / 180));
        float $$9 = Mth.sin(-$$4 * ((float)Math.PI / 180));
        double $$10 = $$0.getX() - (double)$$7 * 0.3;
        double $$11 = $$0.getEyeY();
        double $$12 = $$0.getZ() - (double)$$6 * 0.3;
        this.snapTo($$10, $$11, $$12, $$5, $$4);
        Vec3 $$13 = new Vec3(-$$7, Mth.clamp(-($$9 / $$8), -5.0f, 5.0f), -$$6);
        double $$14 = $$13.length();
        $$13 = $$13.multiply(0.6 / $$14 + this.random.triangle(0.5, 0.0103365), 0.6 / $$14 + this.random.triangle(0.5, 0.0103365), 0.6 / $$14 + this.random.triangle(0.5, 0.0103365));
        this.setDeltaMovement($$13);
        this.setYRot((float)(Mth.atan2($$13.x, $$13.z) * 57.2957763671875));
        this.setXRot((float)(Mth.atan2($$13.y, $$13.horizontalDistance()) * 57.2957763671875));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolationHandler;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_HOOKED_ENTITY, 0);
        $$0.define(DATA_BITING, false);
    }

    @Override
    protected boolean shouldBounceOnWorldBorder() {
        return true;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        if (DATA_HOOKED_ENTITY.equals($$0)) {
            int $$1 = this.getEntityData().get(DATA_HOOKED_ENTITY);
            Entity entity = this.hookedIn = $$1 > 0 ? this.level().getEntity($$1 - 1) : null;
        }
        if (DATA_BITING.equals($$0)) {
            this.biting = this.getEntityData().get(DATA_BITING);
            if (this.biting) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.4f * Mth.nextFloat(this.syncronizedRandom, 0.6f, 1.0f), this.getDeltaMovement().z);
            }
        }
        super.onSyncedDataUpdated($$0);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        double $$1 = 64.0;
        return $$0 < 4096.0;
    }

    @Override
    public void tick() {
        boolean $$4;
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
        this.getInterpolation().interpolate();
        super.tick();
        Player $$0 = this.getPlayerOwner();
        if ($$0 == null) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide() && this.shouldStopFishing($$0)) {
            return;
        }
        if (this.onGround()) {
            ++this.life;
            if (this.life >= 1200) {
                this.discard();
                return;
            }
        } else {
            this.life = 0;
        }
        float $$1 = 0.0f;
        BlockPos $$2 = this.blockPosition();
        FluidState $$3 = this.level().getFluidState($$2);
        if ($$3.is(FluidTags.WATER)) {
            $$1 = $$3.getHeight(this.level(), $$2);
        }
        boolean bl = $$4 = $$1 > 0.0f;
        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                this.setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            }
            if ($$4) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                this.currentState = FishHookState.BOBBING;
                return;
            }
            this.checkCollision();
        } else {
            if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
                if (this.hookedIn != null) {
                    if (this.hookedIn.isRemoved() || !this.hookedIn.canInteractWithLevel() || this.hookedIn.level().dimension() != this.level().dimension()) {
                        this.setHookedEntity(null);
                        this.currentState = FishHookState.FLYING;
                    } else {
                        this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                    }
                }
                return;
            }
            if (this.currentState == FishHookState.BOBBING) {
                Vec3 $$5 = this.getDeltaMovement();
                double $$6 = this.getY() + $$5.y - (double)$$2.getY() - (double)$$1;
                if (Math.abs($$6) < 0.01) {
                    $$6 += Math.signum($$6) * 0.1;
                }
                this.setDeltaMovement($$5.x * 0.9, $$5.y - $$6 * (double)this.random.nextFloat() * 0.2, $$5.z * 0.9);
                this.openWater = this.nibble > 0 || this.timeUntilHooked > 0 ? this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater($$2) : true;
                if ($$4) {
                    this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                    if (this.biting) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.1 * (double)this.syncronizedRandom.nextFloat() * (double)this.syncronizedRandom.nextFloat(), 0.0));
                    }
                    if (!this.level().isClientSide()) {
                        this.catchingFish($$2);
                    }
                } else {
                    this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                }
            }
        }
        if (!$$3.is(FluidTags.WATER) && !this.onGround() && this.hookedIn == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.updateRotation();
        if (this.currentState == FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        double $$7 = 0.92;
        this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
        this.reapplyPosition();
    }

    private boolean shouldStopFishing(Player $$0) {
        if ($$0.canInteractWithLevel()) {
            ItemStack $$1 = $$0.getMainHandItem();
            ItemStack $$2 = $$0.getOffhandItem();
            boolean $$3 = $$1.is(Items.FISHING_ROD);
            boolean $$4 = $$2.is(Items.FISHING_ROD);
            if (($$3 || $$4) && this.distanceToSqr($$0) <= 1024.0) {
                return false;
            }
        }
        this.discard();
        return true;
    }

    private void checkCollision() {
        HitResult $$0 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf($$0);
    }

    @Override
    protected boolean canHitEntity(Entity $$0) {
        return super.canHitEntity($$0) || $$0.isAlive() && $$0 instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult $$0) {
        super.onHitEntity($$0);
        if (!this.level().isClientSide()) {
            this.setHookedEntity($$0.getEntity());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult $$0) {
        super.onHitBlock($$0);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale($$0.distanceTo(this)));
    }

    private void setHookedEntity(@Nullable Entity $$0) {
        this.hookedIn = $$0;
        this.getEntityData().set(DATA_HOOKED_ENTITY, $$0 == null ? 0 : $$0.getId() + 1);
    }

    private void catchingFish(BlockPos $$0) {
        ServerLevel $$1 = (ServerLevel)this.level();
        int $$2 = 1;
        BlockPos $$3 = $$0.above();
        if (this.random.nextFloat() < 0.25f && this.level().isRainingAt($$3)) {
            ++$$2;
        }
        if (this.random.nextFloat() < 0.5f && !this.level().canSeeSky($$3)) {
            --$$2;
        }
        if (this.nibble > 0) {
            --this.nibble;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.getEntityData().set(DATA_BITING, false);
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= $$2;
            if (this.timeUntilHooked > 0) {
                double $$9;
                double $$8;
                this.fishAngle += (float)this.random.triangle(0.0, 9.188);
                float $$4 = this.fishAngle * ((float)Math.PI / 180);
                float $$5 = Mth.sin($$4);
                float $$6 = Mth.cos($$4);
                double $$7 = this.getX() + (double)($$5 * (float)this.timeUntilHooked * 0.1f);
                BlockState $$10 = $$1.getBlockState(BlockPos.containing($$7, ($$8 = (double)((float)Mth.floor(this.getY()) + 1.0f)) - 1.0, $$9 = this.getZ() + (double)($$6 * (float)this.timeUntilHooked * 0.1f)));
                if ($$10.is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15f) {
                        $$1.sendParticles(ParticleTypes.BUBBLE, $$7, $$8 - (double)0.1f, $$9, 1, $$5, 0.1, $$6, 0.0);
                    }
                    float $$11 = $$5 * 0.04f;
                    float $$12 = $$6 * 0.04f;
                    $$1.sendParticles(ParticleTypes.FISHING, $$7, $$8, $$9, 0, $$12, 0.01, -$$11, 1.0);
                    $$1.sendParticles(ParticleTypes.FISHING, $$7, $$8, $$9, 0, -$$12, 0.01, $$11, 1.0);
                }
            } else {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                double $$13 = this.getY() + 0.5;
                $$1.sendParticles(ParticleTypes.BUBBLE, this.getX(), $$13, this.getZ(), (int)(1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2f);
                $$1.sendParticles(ParticleTypes.FISHING, this.getX(), $$13, this.getZ(), (int)(1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2f);
                this.nibble = Mth.nextInt(this.random, 20, 40);
                this.getEntityData().set(DATA_BITING, true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= $$2;
            float $$14 = 0.15f;
            if (this.timeUntilLured < 20) {
                $$14 += (float)(20 - this.timeUntilLured) * 0.05f;
            } else if (this.timeUntilLured < 40) {
                $$14 += (float)(40 - this.timeUntilLured) * 0.02f;
            } else if (this.timeUntilLured < 60) {
                $$14 += (float)(60 - this.timeUntilLured) * 0.01f;
            }
            if (this.random.nextFloat() < $$14) {
                double $$19;
                double $$18;
                float $$15 = Mth.nextFloat(this.random, 0.0f, 360.0f) * ((float)Math.PI / 180);
                float $$16 = Mth.nextFloat(this.random, 25.0f, 60.0f);
                double $$17 = this.getX() + (double)(Mth.sin($$15) * $$16) * 0.1;
                BlockState $$20 = $$1.getBlockState(BlockPos.containing($$17, ($$18 = (double)((float)Mth.floor(this.getY()) + 1.0f)) - 1.0, $$19 = this.getZ() + (double)(Mth.cos($$15) * $$16) * 0.1));
                if ($$20.is(Blocks.WATER)) {
                    $$1.sendParticles(ParticleTypes.SPLASH, $$17, $$18, $$19, 2 + this.random.nextInt(2), 0.1f, 0.0, 0.1f, 0.0);
                }
            }
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0f, 360.0f);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured -= this.lureSpeed;
        }
    }

    private boolean calculateOpenWater(BlockPos $$0) {
        OpenWaterType $$1 = OpenWaterType.INVALID;
        for (int $$2 = -1; $$2 <= 2; ++$$2) {
            OpenWaterType $$3 = this.getOpenWaterTypeForArea($$0.offset(-2, $$2, -2), $$0.offset(2, $$2, 2));
            switch ($$3.ordinal()) {
                case 2: {
                    return false;
                }
                case 0: {
                    if ($$1 != OpenWaterType.INVALID) break;
                    return false;
                }
                case 1: {
                    if ($$1 != OpenWaterType.ABOVE_WATER) break;
                    return false;
                }
            }
            $$1 = $$3;
        }
        return true;
    }

    private OpenWaterType getOpenWaterTypeForArea(BlockPos $$02, BlockPos $$12) {
        return BlockPos.betweenClosedStream($$02, $$12).map(this::getOpenWaterTypeForBlock).reduce(($$0, $$1) -> $$0 == $$1 ? $$0 : OpenWaterType.INVALID).orElse(OpenWaterType.INVALID);
    }

    private OpenWaterType getOpenWaterTypeForBlock(BlockPos $$0) {
        BlockState $$1 = this.level().getBlockState($$0);
        if ($$1.isAir() || $$1.is(Blocks.LILY_PAD)) {
            return OpenWaterType.ABOVE_WATER;
        }
        FluidState $$2 = $$1.getFluidState();
        if ($$2.is(FluidTags.WATER) && $$2.isSource() && $$1.getCollisionShape(this.level(), $$0).isEmpty()) {
            return OpenWaterType.INSIDE_WATER;
        }
        return OpenWaterType.INVALID;
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
    }

    public int retrieve(ItemStack $$0) {
        Player $$1 = this.getPlayerOwner();
        if (this.level().isClientSide() || $$1 == null || this.shouldStopFishing($$1)) {
            return 0;
        }
        int $$2 = 0;
        if (this.hookedIn != null) {
            this.pullEntity(this.hookedIn);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)$$1, $$0, this, Collections.emptyList());
            this.level().broadcastEntityEvent(this, (byte)31);
            $$2 = this.hookedIn instanceof ItemEntity ? 3 : 5;
        } else if (this.nibble > 0) {
            LootParams $$3 = new LootParams.Builder((ServerLevel)this.level()).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.TOOL, $$0).withParameter(LootContextParams.THIS_ENTITY, this).withLuck((float)this.luck + $$1.getLuck()).create(LootContextParamSets.FISHING);
            LootTable $$4 = this.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            ObjectArrayList<ItemStack> $$5 = $$4.getRandomItems($$3);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)$$1, $$0, this, (Collection<ItemStack>)$$5);
            for (ItemStack $$6 : $$5) {
                ItemEntity $$7 = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), $$6);
                double $$8 = $$1.getX() - this.getX();
                double $$9 = $$1.getY() - this.getY();
                double $$10 = $$1.getZ() - this.getZ();
                double $$11 = 0.1;
                $$7.setDeltaMovement($$8 * 0.1, $$9 * 0.1 + Math.sqrt(Math.sqrt($$8 * $$8 + $$9 * $$9 + $$10 * $$10)) * 0.08, $$10 * 0.1);
                this.level().addFreshEntity($$7);
                $$1.level().addFreshEntity(new ExperienceOrb($$1.level(), $$1.getX(), $$1.getY() + 0.5, $$1.getZ() + 0.5, this.random.nextInt(6) + 1));
                if (!$$6.is(ItemTags.FISHES)) continue;
                $$1.awardStat(Stats.FISH_CAUGHT, 1);
            }
            $$2 = 1;
        }
        if (this.onGround()) {
            $$2 = 2;
        }
        this.discard();
        return $$2;
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        Player $$1;
        Entity entity;
        if ($$0 == 31 && this.level().isClientSide() && (entity = this.hookedIn) instanceof Player && ($$1 = (Player)entity).isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }
        super.handleEntityEvent($$0);
    }

    protected void pullEntity(Entity $$0) {
        Entity $$1 = this.getOwner();
        if ($$1 == null) {
            return;
        }
        Vec3 $$2 = new Vec3($$1.getX() - this.getX(), $$1.getY() - this.getY(), $$1.getZ() - this.getZ()).scale(0.1);
        $$0.setDeltaMovement($$0.getDeltaMovement().add($$2));
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason $$0) {
        this.updateOwnerInfo(null);
        super.remove($$0);
    }

    @Override
    public void onClientRemoval() {
        this.updateOwnerInfo(null);
    }

    @Override
    public void setOwner(@Nullable Entity $$0) {
        super.setOwner($$0);
        this.updateOwnerInfo(this);
    }

    private void updateOwnerInfo(@Nullable FishingHook $$0) {
        Player $$1 = this.getPlayerOwner();
        if ($$1 != null) {
            $$1.fishing = $$0;
        }
    }

    public @Nullable Player getPlayerOwner() {
        Player $$1;
        Entity $$0 = this.getOwner();
        return $$0 instanceof Player ? ($$1 = (Player)$$0) : null;
    }

    public @Nullable Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override
    public boolean canUsePortal(boolean $$0) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        Entity $$1 = this.getOwner();
        return new ClientboundAddEntityPacket((Entity)this, $$0, $$1 == null ? this.getId() : $$1.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        if (this.getPlayerOwner() == null) {
            int $$1 = $$0.getData();
            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", (Object)this.level().getEntity($$1), (Object)$$1);
            this.discard();
        }
    }

    static enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;

    }

    static enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;

    }
}

