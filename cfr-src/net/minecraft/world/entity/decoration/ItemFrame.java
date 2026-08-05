/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.decoration;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemFrame
extends HangingEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ROTATION = SynchedEntityData.defineId(ItemFrame.class, EntityDataSerializers.INT);
    public static final int NUM_ROTATIONS = 8;
    private static final float DEPTH = 0.0625f;
    private static final float WIDTH = 0.75f;
    private static final float HEIGHT = 0.75f;
    private static final byte DEFAULT_ROTATION = 0;
    private static final float DEFAULT_DROP_CHANCE = 1.0f;
    private static final boolean DEFAULT_INVISIBLE = false;
    private static final boolean DEFAULT_FIXED = false;
    private float dropChance = 1.0f;
    private boolean fixed = false;

    public ItemFrame(EntityType<? extends ItemFrame> $$0, Level $$1) {
        super((EntityType<? extends HangingEntity>)$$0, $$1);
        this.setInvisible(false);
    }

    public ItemFrame(Level $$0, BlockPos $$1, Direction $$2) {
        this(EntityType.ITEM_FRAME, $$0, $$1, $$2);
    }

    public ItemFrame(EntityType<? extends ItemFrame> $$0, Level $$1, BlockPos $$2, Direction $$3) {
        super((EntityType<? extends HangingEntity>)$$0, $$1, $$2);
        this.setDirection($$3);
        this.setInvisible(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ITEM, ItemStack.EMPTY);
        $$0.define(DATA_ROTATION, 0);
    }

    @Override
    protected void setDirection(Direction $$0) {
        Objects.requireNonNull($$0);
        super.setDirectionRaw($$0);
        if ($$0.getAxis().isHorizontal()) {
            this.setXRot(0.0f);
            this.setYRot($$0.get2DDataValue() * 90);
        } else {
            this.setXRot(-90 * $$0.getAxisDirection().getStep());
            this.setYRot(0.0f);
        }
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.recalculateBoundingBox();
    }

    @Override
    protected final void recalculateBoundingBox() {
        super.recalculateBoundingBox();
        this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos $$0, Direction $$1) {
        return this.createBoundingBox($$0, $$1, this.hasFramedMap());
    }

    @Override
    protected AABB getPopBox() {
        return this.createBoundingBox(this.pos, this.getDirection(), false);
    }

    private AABB createBoundingBox(BlockPos $$0, Direction $$1, boolean $$2) {
        float $$3 = 0.46875f;
        Vec3 $$4 = Vec3.atCenterOf($$0).relative($$1, -0.46875);
        float $$5 = $$2 ? 1.0f : 0.75f;
        float $$6 = $$2 ? 1.0f : 0.75f;
        Direction.Axis $$7 = $$1.getAxis();
        double $$8 = $$7 == Direction.Axis.X ? 0.0625 : (double)$$5;
        double $$9 = $$7 == Direction.Axis.Y ? 0.0625 : (double)$$6;
        double $$10 = $$7 == Direction.Axis.Z ? 0.0625 : (double)$$5;
        return AABB.ofSize($$4, $$8, $$9, $$10);
    }

    @Override
    public boolean survives() {
        if (this.fixed) {
            return true;
        }
        if (this.hasLevelCollision(this.getPopBox())) {
            return false;
        }
        BlockState $$0 = this.level().getBlockState(this.pos.relative(this.getDirection().getOpposite()));
        if (!($$0.isSolid() || this.getDirection().getAxis().isHorizontal() && DiodeBlock.isDiode($$0))) {
            return false;
        }
        return this.canCoexist(true);
    }

    @Override
    public void move(MoverType $$0, Vec3 $$1) {
        if (!this.fixed) {
            super.move($$0, $$1);
        }
    }

    @Override
    public void push(double $$0, double $$1, double $$2) {
        if (!this.fixed) {
            super.push($$0, $$1, $$2);
        }
    }

    @Override
    public void kill(ServerLevel $$0) {
        this.removeFramedMap(this.getItem());
        super.kill($$0);
    }

    private boolean shouldDamageDropItem(DamageSource $$0) {
        return !$$0.is(DamageTypeTags.IS_EXPLOSION) && !this.getItem().isEmpty();
    }

    private static boolean canHurtWhenFixed(DamageSource $$0) {
        return $$0.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || $$0.isCreativePlayer();
    }

    @Override
    public boolean hurtClient(DamageSource $$0) {
        if (this.fixed && !ItemFrame.canHurtWhenFixed($$0)) {
            return false;
        }
        return !this.isInvulnerableToBase($$0);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.fixed) {
            return ItemFrame.canHurtWhenFixed($$1) && super.hurtServer($$0, $$1, $$2);
        }
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        if (this.shouldDamageDropItem($$1)) {
            this.dropItem($$0, $$1.getEntity(), false);
            this.gameEvent(GameEvent.BLOCK_CHANGE, $$1.getEntity());
            this.playSound(this.getRemoveItemSound(), 1.0f, 1.0f);
            return true;
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    public SoundEvent getRemoveItemSound() {
        return SoundEvents.ITEM_FRAME_REMOVE_ITEM;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        double $$1 = 16.0;
        return $$0 < ($$1 *= 64.0 * ItemFrame.getViewScale()) * $$1;
    }

    @Override
    public void dropItem(ServerLevel $$0, @Nullable Entity $$1) {
        this.playSound(this.getBreakSound(), 1.0f, 1.0f);
        this.dropItem($$0, $$1, true);
        this.gameEvent(GameEvent.BLOCK_CHANGE, $$1);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.ITEM_FRAME_BREAK;
    }

    @Override
    public void playPlacementSound() {
        this.playSound(this.getPlaceSound(), 1.0f, 1.0f);
    }

    public SoundEvent getPlaceSound() {
        return SoundEvents.ITEM_FRAME_PLACE;
    }

    private void dropItem(ServerLevel $$0, @Nullable Entity $$1, boolean $$2) {
        Player $$4;
        if (this.fixed) {
            return;
        }
        ItemStack $$3 = this.getItem();
        this.setItem(ItemStack.EMPTY);
        if (!$$0.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            if ($$1 == null) {
                this.removeFramedMap($$3);
            }
            return;
        }
        if ($$1 instanceof Player && ($$4 = (Player)$$1).hasInfiniteMaterials()) {
            this.removeFramedMap($$3);
            return;
        }
        if ($$2) {
            this.spawnAtLocation($$0, this.getFrameItemStack());
        }
        if (!$$3.isEmpty()) {
            $$3 = $$3.copy();
            this.removeFramedMap($$3);
            if (this.random.nextFloat() < this.dropChance) {
                this.spawnAtLocation($$0, $$3);
            }
        }
    }

    private void removeFramedMap(ItemStack $$0) {
        MapItemSavedData $$2;
        MapId $$1 = this.getFramedMapId($$0);
        if ($$1 != null && ($$2 = MapItem.getSavedData($$1, this.level())) != null) {
            $$2.removedFromFrame(this.pos, this.getId());
        }
        $$0.setEntityRepresentation(null);
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public @Nullable MapId getFramedMapId(ItemStack $$0) {
        return $$0.get(DataComponents.MAP_ID);
    }

    public boolean hasFramedMap() {
        return this.getItem().has(DataComponents.MAP_ID);
    }

    public void setItem(ItemStack $$0) {
        this.setItem($$0, true);
    }

    public void setItem(ItemStack $$0, boolean $$1) {
        if (!$$0.isEmpty()) {
            $$0 = $$0.copyWithCount(1);
        }
        this.onItemChanged($$0);
        this.getEntityData().set(DATA_ITEM, $$0);
        if (!$$0.isEmpty()) {
            this.playSound(this.getAddItemSound(), 1.0f, 1.0f);
        }
        if ($$1 && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    public SoundEvent getAddItemSound() {
        return SoundEvents.ITEM_FRAME_ADD_ITEM;
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        if ($$0 == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot($$0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if ($$0.equals(DATA_ITEM)) {
            this.onItemChanged(this.getItem());
        }
    }

    private void onItemChanged(ItemStack $$0) {
        if (!$$0.isEmpty() && $$0.getFrame() != this) {
            $$0.setEntityRepresentation(this);
        }
        this.recalculateBoundingBox();
    }

    public int getRotation() {
        return this.getEntityData().get(DATA_ROTATION);
    }

    public void setRotation(int $$0) {
        this.setRotation($$0, true);
    }

    private void setRotation(int $$0, boolean $$1) {
        this.getEntityData().set(DATA_ROTATION, $$0 % 8);
        if ($$1 && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        ItemStack $$1 = this.getItem();
        if (!$$1.isEmpty()) {
            $$0.store("Item", ItemStack.CODEC, $$1);
        }
        $$0.putByte("ItemRotation", (byte)this.getRotation());
        $$0.putFloat("ItemDropChance", this.dropChance);
        $$0.store("Facing", Direction.LEGACY_ID_CODEC, this.getDirection());
        $$0.putBoolean("Invisible", this.isInvisible());
        $$0.putBoolean("Fixed", this.fixed);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        ItemStack $$1 = $$0.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        ItemStack $$2 = this.getItem();
        if (!$$2.isEmpty() && !ItemStack.matches($$1, $$2)) {
            this.removeFramedMap($$2);
        }
        this.setItem($$1, false);
        this.setRotation($$0.getByteOr("ItemRotation", (byte)0), false);
        this.dropChance = $$0.getFloatOr("ItemDropChance", 1.0f);
        this.setDirection($$0.read("Facing", Direction.LEGACY_ID_CODEC).orElse(Direction.DOWN));
        this.setInvisible($$0.getBooleanOr("Invisible", false));
        this.fixed = $$0.getBooleanOr("Fixed", false);
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        boolean $$4;
        ItemStack $$2 = $$0.getItemInHand($$1);
        boolean $$3 = !this.getItem().isEmpty();
        boolean bl = $$4 = !$$2.isEmpty();
        if (this.fixed) {
            return InteractionResult.PASS;
        }
        if ($$0.level().isClientSide()) {
            return $$3 || $$4 ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!$$3) {
            if ($$4 && !this.isRemoved()) {
                MapItemSavedData $$5 = MapItem.getSavedData($$2, this.level());
                if ($$5 != null && $$5.isTrackedCountOverLimit(256)) {
                    return InteractionResult.FAIL;
                }
                this.setItem($$2);
                this.gameEvent(GameEvent.BLOCK_CHANGE, $$0);
                $$2.consume(1, $$0);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        this.playSound(this.getRotateItemSound(), 1.0f, 1.0f);
        this.setRotation(this.getRotation() + 1);
        this.gameEvent(GameEvent.BLOCK_CHANGE, $$0);
        return InteractionResult.SUCCESS;
    }

    public SoundEvent getRotateItemSound() {
        return SoundEvents.ITEM_FRAME_ROTATE_ITEM;
    }

    public int getAnalogOutput() {
        if (this.getItem().isEmpty()) {
            return 0;
        }
        return this.getRotation() % 8 + 1;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
        return new ClientboundAddEntityPacket((Entity)this, this.getDirection().get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        this.setDirection(Direction.from3DDataValue($$0.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack $$0 = this.getItem();
        if ($$0.isEmpty()) {
            return this.getFrameItemStack();
        }
        return $$0.copy();
    }

    protected ItemStack getFrameItemStack() {
        return new ItemStack(Items.ITEM_FRAME);
    }

    @Override
    public float getVisualRotationYInDegrees() {
        Direction $$0 = this.getDirection();
        int $$1 = $$0.getAxis().isVertical() ? 90 * $$0.getAxisDirection().getStep() : 0;
        return Mth.wrapDegrees(180 + $$0.get2DDataValue() * 90 + this.getRotation() * 45 + $$1);
    }
}

