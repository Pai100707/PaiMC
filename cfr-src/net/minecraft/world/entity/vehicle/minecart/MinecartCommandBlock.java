/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinecartCommandBlock
extends AbstractMinecart {
    static final EntityDataAccessor<String> DATA_ID_COMMAND_NAME = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.STRING);
    static final EntityDataAccessor<Component> DATA_ID_LAST_OUTPUT = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.COMPONENT);
    private final BaseCommandBlock commandBlock = new MinecartCommandBase();
    private static final int ACTIVATION_DELAY = 4;
    private int lastActivated;

    public MinecartCommandBlock(EntityType<? extends MinecartCommandBlock> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.COMMAND_BLOCK_MINECART);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_COMMAND_NAME, "");
        $$0.define(DATA_ID_LAST_OUTPUT, CommonComponents.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.commandBlock.load($$0);
        this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommandBlock().getCommand());
        this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getCommandBlock().getLastOutput());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        this.commandBlock.save($$0);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.COMMAND_BLOCK.defaultBlockState();
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override
    public void activateMinecart(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4) {
        if ($$4 && this.tickCount - this.lastActivated >= 4) {
            this.getCommandBlock().performCommand($$0);
            this.lastActivated = this.tickCount;
        }
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        if (!$$0.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        }
        if ($$0.level().isClientSide()) {
            $$0.openMinecartCommandBlock(this);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_ID_LAST_OUTPUT.equals($$0)) {
            try {
                this.commandBlock.setLastOutput(this.getEntityData().get(DATA_ID_LAST_OUTPUT));
            }
            catch (Throwable throwable) {}
        } else if (DATA_ID_COMMAND_NAME.equals($$0)) {
            this.commandBlock.setCommand(this.getEntityData().get(DATA_ID_COMMAND_NAME));
        }
    }

    class MinecartCommandBase
    extends BaseCommandBlock {
        MinecartCommandBase() {
        }

        @Override
        public void onUpdated(ServerLevel $$0) {
            MinecartCommandBlock.this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommand());
            MinecartCommandBlock.this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getLastOutput());
        }

        @Override
        public CommandSourceStack createCommandSourceStack(ServerLevel $$0, CommandSource $$1) {
            return new CommandSourceStack($$1, MinecartCommandBlock.this.position(), MinecartCommandBlock.this.getRotationVector(), $$0, LevelBasedPermissionSet.GAMEMASTER, this.getName().getString(), MinecartCommandBlock.this.getDisplayName(), $$0.getServer(), MinecartCommandBlock.this);
        }

        @Override
        public boolean isValid() {
            return !MinecartCommandBlock.this.isRemoved();
        }
    }
}

