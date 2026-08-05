/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.item;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemEntity
extends Entity
implements TraceableEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(ItemEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final float FLOAT_HEIGHT = 0.1f;
    public static final float EYE_HEIGHT = 0.2125f;
    private static final int LIFETIME = 6000;
    private static final int INFINITE_PICKUP_DELAY = Short.MAX_VALUE;
    private static final int INFINITE_LIFETIME = Short.MIN_VALUE;
    private static final int DEFAULT_HEALTH = 5;
    private static final short DEFAULT_AGE = 0;
    private static final short DEFAULT_PICKUP_DELAY = 0;
    private int age = 0;
    private int pickupDelay = 0;
    private int health = 5;
    private @Nullable EntityReference<Entity> thrower;
    private @Nullable UUID target;
    public final float bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0f;

    public ItemEntity(EntityType<? extends ItemEntity> $$0, Level $$1) {
        super($$0, $$1);
        this.setYRot(this.random.nextFloat() * 360.0f);
    }

    public ItemEntity(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        this($$0, $$1, $$2, $$3, $$4, $$0.random.nextDouble() * 0.2 - 0.1, 0.2, $$0.random.nextDouble() * 0.2 - 0.1);
    }

    public ItemEntity(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4, double $$5, double $$6, double $$7) {
        this((EntityType<? extends ItemEntity>)EntityType.ITEM, $$0);
        this.setPos($$1, $$2, $$3);
        this.setDeltaMovement($$5, $$6, $$7);
        this.setItem($$4);
    }

    @Override
    public boolean dampensVibrations() {
        return this.getItem().is(ItemTags.DAMPENS_VIBRATIONS);
    }

    @Override
    public @Nullable Entity getOwner() {
        return EntityReference.getEntity(this.thrower, this.level());
    }

    @Override
    public void restoreFrom(Entity $$0) {
        super.restoreFrom($$0);
        if ($$0 instanceof ItemEntity) {
            ItemEntity $$1 = (ItemEntity)$$0;
            this.thrower = $$1.thrower;
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        double $$5;
        int $$4;
        if (this.getItem().isEmpty()) {
            this.discard();
            return;
        }
        super.tick();
        if (this.pickupDelay > 0 && this.pickupDelay != Short.MAX_VALUE) {
            --this.pickupDelay;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 $$0 = this.getDeltaMovement();
        if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)0.1f) {
            this.setUnderwaterMovement();
        } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)0.1f) {
            this.setUnderLavaMovement();
        } else {
            this.applyGravity();
        }
        if (this.level().isClientSide()) {
            this.noPhysics = false;
        } else {
            boolean bl = this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if (this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }
        if (!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > (double)1.0E-5f || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            float $$1 = 0.98f;
            if (this.onGround()) {
                $$1 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98f;
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply($$1, 0.98, $$1));
            if (this.onGround()) {
                Vec3 $$2 = this.getDeltaMovement();
                if ($$2.y < 0.0) {
                    this.setDeltaMovement($$2.multiply(1.0, -0.5, 1.0));
                }
            }
        }
        boolean $$3 = Mth.floor(this.xo) != Mth.floor(this.getX()) || Mth.floor(this.yo) != Mth.floor(this.getY()) || Mth.floor(this.zo) != Mth.floor(this.getZ());
        int n = $$4 = $$3 ? 2 : 40;
        if (this.tickCount % $$4 == 0 && !this.level().isClientSide() && this.isMergable()) {
            this.mergeWithNeighbours();
        }
        if (this.age != Short.MIN_VALUE) {
            ++this.age;
        }
        this.needsSync |= this.updateInWaterStateAndDoFluidPushing();
        if (!this.level().isClientSide() && ($$5 = this.getDeltaMovement().subtract($$0).lengthSqr()) > 0.01) {
            this.needsSync = true;
        }
        if (!this.level().isClientSide() && this.age >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999f);
    }

    private void setUnderwaterMovement() {
        this.setFluidMovement(0.99f);
    }

    private void setUnderLavaMovement() {
        this.setFluidMovement(0.95f);
    }

    private void setFluidMovement(double $$0) {
        Vec3 $$1 = this.getDeltaMovement();
        this.setDeltaMovement($$1.x * $$0, $$1.y + (double)($$1.y < (double)0.06f ? 5.0E-4f : 0.0f), $$1.z * $$0);
    }

    private void mergeWithNeighbours() {
        if (!this.isMergable()) {
            return;
        }
        List<ItemEntity> $$02 = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5, 0.0, 0.5), $$0 -> $$0 != this && $$0.isMergable());
        for (ItemEntity $$1 : $$02) {
            if (!$$1.isMergable()) continue;
            this.tryToMerge($$1);
            if (!this.isRemoved()) continue;
            break;
        }
    }

    private boolean isMergable() {
        ItemStack $$0 = this.getItem();
        return this.isAlive() && this.pickupDelay != Short.MAX_VALUE && this.age != Short.MIN_VALUE && this.age < 6000 && $$0.getCount() < $$0.getMaxStackSize();
    }

    private void tryToMerge(ItemEntity $$0) {
        ItemStack $$1 = this.getItem();
        ItemStack $$2 = $$0.getItem();
        if (!Objects.equals(this.target, $$0.target) || !ItemEntity.areMergable($$1, $$2)) {
            return;
        }
        if ($$2.getCount() < $$1.getCount()) {
            ItemEntity.merge(this, $$1, $$0, $$2);
        } else {
            ItemEntity.merge($$0, $$2, this, $$1);
        }
    }

    public static boolean areMergable(ItemStack $$0, ItemStack $$1) {
        if ($$1.getCount() + $$0.getCount() > $$1.getMaxStackSize()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents($$0, $$1);
    }

    public static ItemStack merge(ItemStack $$0, ItemStack $$1, int $$2) {
        int $$3 = Math.min(Math.min($$0.getMaxStackSize(), $$2) - $$0.getCount(), $$1.getCount());
        ItemStack $$4 = $$0.copyWithCount($$0.getCount() + $$3);
        $$1.shrink($$3);
        return $$4;
    }

    private static void merge(ItemEntity $$0, ItemStack $$1, ItemStack $$2) {
        ItemStack $$3 = ItemEntity.merge($$1, $$2, 64);
        $$0.setItem($$3);
    }

    private static void merge(ItemEntity $$0, ItemStack $$1, ItemEntity $$2, ItemStack $$3) {
        ItemEntity.merge($$0, $$1, $$3);
        $$0.pickupDelay = Math.max($$0.pickupDelay, $$2.pickupDelay);
        $$0.age = Math.min($$0.age, $$2.age);
        if ($$3.isEmpty()) {
            $$2.discard();
        }
    }

    @Override
    public boolean fireImmune() {
        return !this.getItem().canBeHurtBy(this.damageSources().inFire()) || super.fireImmune();
    }

    @Override
    protected boolean shouldPlayLavaHurtSound() {
        if (this.health <= 0) {
            return true;
        }
        return this.tickCount % 10 == 0;
    }

    @Override
    public final boolean hurtClient(DamageSource $$0) {
        if (this.isInvulnerableToBase($$0)) {
            return false;
        }
        return this.getItem().canBeHurtBy($$0);
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        if (!$$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue() && $$1.getEntity() instanceof Mob) {
            return false;
        }
        if (!this.getItem().canBeHurtBy($$1)) {
            return false;
        }
        this.markHurt();
        this.health = (int)((float)this.health - $$2);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
        if (this.health <= 0) {
            this.getItem().onDestroyed(this);
            this.discard();
        }
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion $$0) {
        if ($$0.shouldAffectBlocklikeEntities()) {
            return super.ignoreExplosion($$0);
        }
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.putShort("Health", (short)this.health);
        $$0.putShort("Age", (short)this.age);
        $$0.putShort("PickupDelay", (short)this.pickupDelay);
        EntityReference.store(this.thrower, $$0, "Thrower");
        $$0.storeNullable("Owner", UUIDUtil.CODEC, this.target);
        if (!this.getItem().isEmpty()) {
            $$0.store("Item", ItemStack.CODEC, this.getItem());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.health = $$0.getShortOr("Health", (short)5);
        this.age = $$0.getShortOr("Age", (short)0);
        this.pickupDelay = $$0.getShortOr("PickupDelay", (short)0);
        this.target = $$0.read("Owner", UUIDUtil.CODEC).orElse(null);
        this.thrower = EntityReference.read($$0, "Thrower");
        this.setItem($$0.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        if (this.getItem().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public void playerTouch(Player $$0) {
        if (this.level().isClientSide()) {
            return;
        }
        ItemStack $$1 = this.getItem();
        Item $$2 = $$1.getItem();
        int $$3 = $$1.getCount();
        if (this.pickupDelay == 0 && (this.target == null || this.target.equals($$0.getUUID())) && $$0.getInventory().add($$1)) {
            $$0.take(this, $$3);
            if ($$1.isEmpty()) {
                this.discard();
                $$1.setCount($$3);
            }
            $$0.awardStat(Stats.ITEM_PICKED_UP.get($$2), $$3);
            $$0.onItemPickup(this);
        }
    }

    @Override
    public Component getName() {
        Component $$0 = this.getCustomName();
        if ($$0 != null) {
            return $$0;
        }
        return this.getItem().getItemName();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public @Nullable Entity teleport(TeleportTransition $$0) {
        Entity $$1 = super.teleport($$0);
        if (!this.level().isClientSide() && $$1 instanceof ItemEntity) {
            ItemEntity $$2 = (ItemEntity)$$1;
            $$2.mergeWithNeighbours();
        }
        return $$1;
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    public void setItem(ItemStack $$0) {
        this.getEntityData().set(DATA_ITEM, $$0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_ITEM.equals($$0)) {
            this.getItem().setEntityRepresentation(this);
        }
    }

    public void setTarget(@Nullable UUID $$0) {
        this.target = $$0;
    }

    public void setThrower(Entity $$0) {
        this.thrower = EntityReference.of($$0);
    }

    public int getAge() {
        return this.age;
    }

    public void setDefaultPickUpDelay() {
        this.pickupDelay = 10;
    }

    public void setNoPickUpDelay() {
        this.pickupDelay = 0;
    }

    public void setNeverPickUp() {
        this.pickupDelay = Short.MAX_VALUE;
    }

    public void setPickUpDelay(int $$0) {
        this.pickupDelay = $$0;
    }

    public boolean hasPickUpDelay() {
        return this.pickupDelay > 0;
    }

    public void setUnlimitedLifetime() {
        this.age = Short.MIN_VALUE;
    }

    public void setExtendedLifetime() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setNeverPickUp();
        this.age = 5999;
    }

    public static float getSpin(float $$0, float $$1) {
        return $$0 / 20.0f + $$1;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0f - ItemEntity.getSpin((float)this.getAge() + 0.5f, this.bobOffs) / ((float)Math.PI * 2) * 360.0f;
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        if ($$0 == 0) {
            return SlotAccess.of(this::getItem, this::setItem);
        }
        return super.getSlot($$0);
    }
}

