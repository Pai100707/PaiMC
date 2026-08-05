/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.SAND.defaultBlockState();
    private static final int DEFAULT_TIME = 0;
    private static final float DEFAULT_FALL_DAMAGE_PER_DISTANCE = 0.0f;
    private static final int DEFAULT_MAX_FALL_DAMAGE = 40;
    private static final boolean DEFAULT_DROP_ITEM = true;
    private static final boolean DEFAULT_CANCEL_DROP = false;
    private BlockState blockState = DEFAULT_BLOCK_STATE;
    public int time = 0;
    public boolean dropItem = true;
    private boolean cancelDrop = false;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance = 0.0f;
    public @Nullable CompoundTag blockData;
    public boolean forceTickAfterTeleportToDuplicate;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    private FallingBlockEntity(Level $$0, double $$1, double $$2, double $$3, BlockState $$4) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, $$0);
        this.blockState = $$4;
        this.blocksBuilding = true;
        this.setPos($$1, $$2, $$3);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = $$1;
        this.yo = $$2;
        this.zo = $$3;
        this.setStartPos(this.blockPosition());
    }

    public static FallingBlockEntity fall(Level $$0, BlockPos $$1, BlockState $$2) {
        FallingBlockEntity $$3 = new FallingBlockEntity($$0, (double)$$1.getX() + 0.5, $$1.getY(), (double)$$1.getZ() + 0.5, $$2.hasProperty(BlockStateProperties.WATERLOGGED) ? (BlockState)$$2.setValue(BlockStateProperties.WATERLOGGED, false) : $$2);
        $$0.setBlock($$1, $$2.getFluidState().createLegacyBlock(), 3);
        $$0.addFreshEntity($$3);
        return $$3;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (!this.isInvulnerableToBase($$1)) {
            this.markHurt();
        }
        return false;
    }

    public void setStartPos(BlockPos $$0) {
        this.entityData.set(DATA_START_POS, $$0);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
            return;
        }
        Block $$0 = this.blockState.getBlock();
        ++this.time;
        this.applyGravity();
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.handlePortal();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$12 = (ServerLevel)level;
            if (this.isAlive() || this.forceTickAfterTeleportToDuplicate) {
                BlockHitResult $$6;
                BlockPos $$22 = this.blockPosition();
                boolean $$3 = this.blockState.getBlock() instanceof ConcretePowderBlock;
                boolean $$4 = $$3 && this.level().getFluidState($$22).is(FluidTags.WATER);
                double $$5 = this.getDeltaMovement().lengthSqr();
                if ($$3 && $$5 > 1.0 && ($$6 = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this))).getType() != HitResult.Type.MISS && this.level().getFluidState($$6.getBlockPos()).is(FluidTags.WATER)) {
                    $$22 = $$6.getBlockPos();
                    $$4 = true;
                }
                if (this.onGround() || $$4) {
                    BlockState $$7 = this.level().getBlockState($$22);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                    if (!$$7.is(Blocks.MOVING_PISTON)) {
                        if (!this.cancelDrop) {
                            boolean $$10;
                            boolean $$8 = $$7.canBeReplaced(new DirectionalPlaceContext(this.level(), $$22, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                            boolean $$9 = FallingBlock.isFree(this.level().getBlockState($$22.below())) && (!$$3 || !$$4);
                            boolean bl = $$10 = this.blockState.canSurvive(this.level(), $$22) && !$$9;
                            if ($$8 && $$10) {
                                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState($$22).getType() == Fluids.WATER) {
                                    this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                                }
                                if (this.level().setBlock($$22, this.blockState, 3)) {
                                    BlockEntity $$122;
                                    $$12.getChunkSource().chunkMap.sendToTrackingPlayers(this, new ClientboundBlockUpdatePacket($$22, this.level().getBlockState($$22)));
                                    this.discard();
                                    if ($$0 instanceof Fallable) {
                                        Fallable $$11 = (Fallable)((Object)$$0);
                                        $$11.onLand(this.level(), $$22, this.blockState, $$7, this);
                                    }
                                    if (this.blockData != null && this.blockState.hasBlockEntity() && ($$122 = this.level().getBlockEntity($$22)) != null) {
                                        try (ProblemReporter.ScopedCollector $$13 = new ProblemReporter.ScopedCollector($$122.problemPath(), LOGGER);){
                                            RegistryAccess $$14 = this.level().registryAccess();
                                            TagValueOutput $$15 = TagValueOutput.createWithContext($$13, $$14);
                                            $$122.saveWithoutMetadata($$15);
                                            CompoundTag $$16 = $$15.buildResult();
                                            this.blockData.forEach(($$1, $$2) -> $$16.put((String)$$1, $$2.copy()));
                                            $$122.loadWithComponents(TagValueInput.create((ProblemReporter)$$13, (HolderLookup.Provider)$$14, $$16));
                                        }
                                        catch (Exception $$17) {
                                            LOGGER.error("Failed to load block entity from falling block", (Throwable)$$17);
                                        }
                                        $$122.setChanged();
                                    }
                                } else if (this.dropItem && $$12.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
                                    this.discard();
                                    this.callOnBrokenAfterFall($$0, $$22);
                                    this.spawnAtLocation($$12, $$0);
                                }
                            } else {
                                this.discard();
                                if (this.dropItem && $$12.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
                                    this.callOnBrokenAfterFall($$0, $$22);
                                    this.spawnAtLocation($$12, $$0);
                                }
                            }
                        } else {
                            this.discard();
                            this.callOnBrokenAfterFall($$0, $$22);
                        }
                    }
                } else if (this.time > 100 && ($$22.getY() <= this.level().getMinY() || $$22.getY() > this.level().getMaxY()) || this.time > 600) {
                    if (this.dropItem && $$12.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
                        this.spawnAtLocation($$12, $$0);
                    }
                    this.discard();
                }
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    public void callOnBrokenAfterFall(Block $$0, BlockPos $$1) {
        if ($$0 instanceof Fallable) {
            ((Fallable)((Object)$$0)).onBrokenAfterFall(this.level(), $$1, this);
        }
    }

    @Override
    public boolean causeFallDamage(double $$0, float $$1, DamageSource $$22) {
        DamageSource damageSource;
        if (!this.hurtEntities) {
            return false;
        }
        int $$3 = Mth.ceil($$0 - 1.0);
        if ($$3 < 0) {
            return false;
        }
        Predicate<Entity> $$4 = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        Block block = this.blockState.getBlock();
        if (block instanceof Fallable) {
            Fallable $$5 = (Fallable)((Object)block);
            damageSource = $$5.getFallDamageSource(this);
        } else {
            damageSource = this.damageSources().fallingBlock(this);
        }
        DamageSource $$6 = damageSource;
        float $$7 = Math.min(Mth.floor((float)$$3 * this.fallDamagePerDistance), this.fallDamageMax);
        this.level().getEntities(this, this.getBoundingBox(), $$4).forEach($$2 -> $$2.hurt($$6, $$7));
        boolean $$8 = this.blockState.is(BlockTags.ANVIL);
        if ($$8 && $$7 > 0.0f && this.random.nextFloat() < 0.05f + (float)$$3 * 0.05f) {
            BlockState $$9 = AnvilBlock.damage(this.blockState);
            if ($$9 == null) {
                this.cancelDrop = true;
            } else {
                this.blockState = $$9;
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.store("BlockState", BlockState.CODEC, this.blockState);
        $$0.putInt("Time", this.time);
        $$0.putBoolean("DropItem", this.dropItem);
        $$0.putBoolean("HurtEntities", this.hurtEntities);
        $$0.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        $$0.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            $$0.store("TileEntityData", CompoundTag.CODEC, this.blockData);
        }
        $$0.putBoolean("CancelDrop", this.cancelDrop);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.blockState = $$0.read("BlockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
        this.time = $$0.getIntOr("Time", 0);
        boolean $$1 = this.blockState.is(BlockTags.ANVIL);
        this.hurtEntities = $$0.getBooleanOr("HurtEntities", $$1);
        this.fallDamagePerDistance = $$0.getFloatOr("FallHurtAmount", 0.0f);
        this.fallDamageMax = $$0.getIntOr("FallHurtMax", 40);
        this.dropItem = $$0.getBooleanOr("DropItem", true);
        this.blockData = $$0.read("TileEntityData", CompoundTag.CODEC).orElse(null);
        this.cancelDrop = $$0.getBooleanOr("CancelDrop", false);
    }

    public void setHurtsEntities(float $$0, int $$1) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = $$0;
        this.fallDamageMax = $$1;
    }

    public void disableDrop() {
        this.cancelDrop = true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory $$0) {
        super.fillCrashReportCategory($$0);
        $$0.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        return new ClientboundAddEntityPacket((Entity)this, $$0, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        this.blockState = Block.stateById($$0.getData());
        this.blocksBuilding = true;
        double $$1 = $$0.getX();
        double $$2 = $$0.getY();
        double $$3 = $$0.getZ();
        this.setPos($$1, $$2, $$3);
        this.setStartPos(this.blockPosition());
    }

    @Override
    public @Nullable Entity teleport(TeleportTransition $$0) {
        ResourceKey<Level> $$1 = $$0.newLevel().dimension();
        ResourceKey<Level> $$2 = this.level().dimension();
        boolean $$3 = ($$2 == Level.END || $$1 == Level.END) && $$2 != $$1;
        Entity $$4 = super.teleport($$0);
        this.forceTickAfterTeleportToDuplicate = $$4 != null && $$3;
        return $$4;
    }
}

