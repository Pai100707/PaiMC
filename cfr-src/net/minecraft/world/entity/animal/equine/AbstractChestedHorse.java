/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChestedHorse
extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_HAS_CHEST = false;
    private final EntityDimensions babyDimensions;

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> $$0, Level $$1) {
        super((EntityType<? extends AbstractHorse>)$$0, $$1);
        this.canGallop = false;
        this.babyDimensions = $$0.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, $$0.getHeight() - 0.15625f, 0.0f)).scale(0.5f);
    }

    @Override
    protected void randomizeAttributes(RandomSource $$0) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(AbstractChestedHorse.generateMaxHealth($$0::nextInt));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return AbstractChestedHorse.createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175f).add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_ID_CHEST);
    }

    public void setChest(boolean $$0) {
        this.entityData.set(DATA_ID_CHEST, $$0);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? this.babyDimensions : super.getDefaultDimensions($$0);
    }

    @Override
    protected void dropEquipment(ServerLevel $$0) {
        super.dropEquipment($$0);
        if (this.hasChest()) {
            this.spawnAtLocation($$0, Blocks.CHEST);
            this.setChest(false);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            ValueOutput.TypedOutputList<ItemStackWithSlot> $$1 = $$0.list("Items", ItemStackWithSlot.CODEC);
            for (int $$2 = 0; $$2 < this.inventory.getContainerSize(); ++$$2) {
                ItemStack $$3 = this.inventory.getItem($$2);
                if ($$3.isEmpty()) continue;
                $$1.add(new ItemStackWithSlot($$2, $$3));
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setChest($$0.getBooleanOr("ChestedHorse", false));
        this.createInventory();
        if (this.hasChest()) {
            for (ItemStackWithSlot $$1 : $$0.listOrEmpty("Items", ItemStackWithSlot.CODEC)) {
                if (!$$1.isValidInContainer(this.inventory.getContainerSize())) continue;
                this.inventory.setItem($$1.slot(), $$1.stack());
            }
        }
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        if ($$0 == 499) {
            return new SlotAccess(){

                @Override
                public ItemStack get() {
                    return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
                }

                @Override
                public boolean set(ItemStack $$0) {
                    if ($$0.isEmpty()) {
                        if (AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(false);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    if ($$0.is(Items.CHEST)) {
                        if (!AbstractChestedHorse.this.hasChest()) {
                            AbstractChestedHorse.this.setChest(true);
                            AbstractChestedHorse.this.createInventory();
                        }
                        return true;
                    }
                    return false;
                }
            };
        }
        return super.getSlot($$0);
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
            if (!this.hasChest() && $$3.is(Items.CHEST)) {
                this.equipChest($$0, $$3);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract($$0, $$1);
    }

    private void equipChest(Player $$0, ItemStack $$1) {
        this.setChest(true);
        this.playChestEquipsSound();
        $$1.consume(1, $$0);
        this.createInventory();
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.41, 0.18, 0.73);
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}

