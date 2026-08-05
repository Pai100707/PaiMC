/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.util.Map;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public class BlockItem
extends Item {
    @Deprecated
    private final Block block;

    public BlockItem(Block $$0, Item.Properties $$1) {
        super($$1);
        this.block = $$0;
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        InteractionResult $$1 = this.place(new BlockPlaceContext($$0));
        if (!$$1.consumesAction() && $$0.getItemInHand().has(DataComponents.CONSUMABLE)) {
            return super.use($$0.getLevel(), $$0.getPlayer(), $$0.getHand());
        }
        return $$1;
    }

    public InteractionResult place(BlockPlaceContext $$0) {
        if (!this.getBlock().isEnabled($$0.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        if (!$$0.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext $$1 = this.updatePlacementContext($$0);
        if ($$1 == null) {
            return InteractionResult.FAIL;
        }
        BlockState $$2 = this.getPlacementState($$1);
        if ($$2 == null) {
            return InteractionResult.FAIL;
        }
        if (!this.placeBlock($$1, $$2)) {
            return InteractionResult.FAIL;
        }
        BlockPos $$3 = $$1.getClickedPos();
        Level $$4 = $$1.getLevel();
        Player $$5 = $$1.getPlayer();
        ItemStack $$6 = $$1.getItemInHand();
        BlockState $$7 = $$4.getBlockState($$3);
        if ($$7.is($$2.getBlock())) {
            $$7 = this.updateBlockStateFromTag($$3, $$4, $$6, $$7);
            this.updateCustomBlockEntityTag($$3, $$4, $$5, $$6, $$7);
            BlockItem.updateBlockEntityComponents($$4, $$3, $$6);
            $$7.getBlock().setPlacedBy($$4, $$3, $$7, $$5, $$6);
            if ($$5 instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)$$5, $$3, $$6);
            }
        }
        SoundType $$8 = $$7.getSoundType();
        $$4.playSound((Entity)$$5, $$3, this.getPlaceSound($$7), SoundSource.BLOCKS, ($$8.getVolume() + 1.0f) / 2.0f, $$8.getPitch() * 0.8f);
        $$4.gameEvent(GameEvent.BLOCK_PLACE, $$3, GameEvent.Context.of($$5, $$7));
        $$6.consume(1, $$5);
        return InteractionResult.SUCCESS;
    }

    protected SoundEvent getPlaceSound(BlockState $$0) {
        return $$0.getSoundType().getPlaceSound();
    }

    public @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext $$0) {
        return $$0;
    }

    private static void updateBlockEntityComponents(Level $$0, BlockPos $$1, ItemStack $$2) {
        BlockEntity $$3 = $$0.getBlockEntity($$1);
        if ($$3 != null) {
            $$3.applyComponentsFromItemStack($$2);
            $$3.setChanged();
        }
    }

    protected boolean updateCustomBlockEntityTag(BlockPos $$0, Level $$1, @Nullable Player $$2, ItemStack $$3, BlockState $$4) {
        return BlockItem.updateCustomBlockEntityTag($$1, $$2, $$0, $$3);
    }

    protected @Nullable BlockState getPlacementState(BlockPlaceContext $$0) {
        BlockState $$1 = this.getBlock().getStateForPlacement($$0);
        return $$1 != null && this.canPlace($$0, $$1) ? $$1 : null;
    }

    private BlockState updateBlockStateFromTag(BlockPos $$0, Level $$1, ItemStack $$2, BlockState $$3) {
        BlockItemStateProperties $$4 = $$2.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        if ($$4.isEmpty()) {
            return $$3;
        }
        BlockState $$5 = $$4.apply($$3);
        if ($$5 != $$3) {
            $$1.setBlock($$0, $$5, 2);
        }
        return $$5;
    }

    protected boolean canPlace(BlockPlaceContext $$0, BlockState $$1) {
        Player $$2 = $$0.getPlayer();
        return (!this.mustSurvive() || $$1.canSurvive($$0.getLevel(), $$0.getClickedPos())) && $$0.getLevel().isUnobstructed($$1, $$0.getClickedPos(), CollisionContext.placementContext($$2));
    }

    protected boolean mustSurvive() {
        return true;
    }

    protected boolean placeBlock(BlockPlaceContext $$0, BlockState $$1) {
        return $$0.getLevel().setBlock($$0.getClickedPos(), $$1, 11);
    }

    public static boolean updateCustomBlockEntityTag(Level $$0, @Nullable Player $$1, BlockPos $$2, ItemStack $$3) {
        BlockEntity $$5;
        if ($$0.isClientSide()) {
            return false;
        }
        TypedEntityData<BlockEntityType<?>> $$4 = $$3.get(DataComponents.BLOCK_ENTITY_DATA);
        if ($$4 != null && ($$5 = $$0.getBlockEntity($$2)) != null) {
            BlockEntityType<?> $$6 = $$5.getType();
            if ($$6 != $$4.type()) {
                return false;
            }
            if ($$6.onlyOpCanSetNbt() && ($$1 == null || !$$1.canUseGameMasterBlocks())) {
                return false;
            }
            return $$4.loadInto($$5, $$0.registryAccess());
        }
        return false;
    }

    @Override
    public boolean shouldPrintOpWarning(ItemStack $$0, @Nullable Player $$1) {
        TypedEntityData<BlockEntityType<?>> $$2;
        if ($$1 != null && $$1.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && ($$2 = $$0.get(DataComponents.BLOCK_ENTITY_DATA)) != null) {
            return $$2.type().onlyOpCanSetNbt();
        }
        return false;
    }

    public Block getBlock() {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> $$0, Item $$1) {
        $$0.put(this.getBlock(), $$1);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return !(this.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    public void onDestroyed(ItemEntity $$0) {
        ItemContainerContents $$1 = $$0.getItem().set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        if ($$1 != null) {
            ItemUtils.onContainerDestroyed($$0, $$1.nonEmptyItemsCopy());
        }
    }

    public static void setBlockEntityData(ItemStack $$0, BlockEntityType<?> $$1, TagValueOutput $$2) {
        $$2.discard("id");
        if ($$2.isEmpty()) {
            $$0.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            BlockEntity.addEntityType($$2, $$1);
            $$0.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of($$1, $$2.buildResult()));
        }
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.getBlock().requiredFeatures();
    }
}

