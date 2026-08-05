/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

public class AxeItem
extends Item {
    protected static final Map<Block, Block> STRIPPABLES = new ImmutableMap.Builder().put((Object)Blocks.OAK_WOOD, (Object)Blocks.STRIPPED_OAK_WOOD).put((Object)Blocks.OAK_LOG, (Object)Blocks.STRIPPED_OAK_LOG).put((Object)Blocks.DARK_OAK_WOOD, (Object)Blocks.STRIPPED_DARK_OAK_WOOD).put((Object)Blocks.DARK_OAK_LOG, (Object)Blocks.STRIPPED_DARK_OAK_LOG).put((Object)Blocks.PALE_OAK_WOOD, (Object)Blocks.STRIPPED_PALE_OAK_WOOD).put((Object)Blocks.PALE_OAK_LOG, (Object)Blocks.STRIPPED_PALE_OAK_LOG).put((Object)Blocks.ACACIA_WOOD, (Object)Blocks.STRIPPED_ACACIA_WOOD).put((Object)Blocks.ACACIA_LOG, (Object)Blocks.STRIPPED_ACACIA_LOG).put((Object)Blocks.CHERRY_WOOD, (Object)Blocks.STRIPPED_CHERRY_WOOD).put((Object)Blocks.CHERRY_LOG, (Object)Blocks.STRIPPED_CHERRY_LOG).put((Object)Blocks.BIRCH_WOOD, (Object)Blocks.STRIPPED_BIRCH_WOOD).put((Object)Blocks.BIRCH_LOG, (Object)Blocks.STRIPPED_BIRCH_LOG).put((Object)Blocks.JUNGLE_WOOD, (Object)Blocks.STRIPPED_JUNGLE_WOOD).put((Object)Blocks.JUNGLE_LOG, (Object)Blocks.STRIPPED_JUNGLE_LOG).put((Object)Blocks.SPRUCE_WOOD, (Object)Blocks.STRIPPED_SPRUCE_WOOD).put((Object)Blocks.SPRUCE_LOG, (Object)Blocks.STRIPPED_SPRUCE_LOG).put((Object)Blocks.WARPED_STEM, (Object)Blocks.STRIPPED_WARPED_STEM).put((Object)Blocks.WARPED_HYPHAE, (Object)Blocks.STRIPPED_WARPED_HYPHAE).put((Object)Blocks.CRIMSON_STEM, (Object)Blocks.STRIPPED_CRIMSON_STEM).put((Object)Blocks.CRIMSON_HYPHAE, (Object)Blocks.STRIPPED_CRIMSON_HYPHAE).put((Object)Blocks.MANGROVE_WOOD, (Object)Blocks.STRIPPED_MANGROVE_WOOD).put((Object)Blocks.MANGROVE_LOG, (Object)Blocks.STRIPPED_MANGROVE_LOG).put((Object)Blocks.BAMBOO_BLOCK, (Object)Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public AxeItem(ToolMaterial $$0, float $$1, float $$2, Item.Properties $$3) {
        super($$3.axe($$0, $$1, $$2));
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        Level $$1 = $$0.getLevel();
        BlockPos $$2 = $$0.getClickedPos();
        Player $$3 = $$0.getPlayer();
        if (AxeItem.playerHasBlockingItemUseIntent($$0)) {
            return InteractionResult.PASS;
        }
        Optional<BlockState> $$4 = this.evaluateNewBlockState($$1, $$2, $$3, $$1.getBlockState($$2));
        if ($$4.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack $$5 = $$0.getItemInHand();
        if ($$3 instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)$$3, $$2, $$5);
        }
        $$1.setBlock($$2, $$4.get(), 11);
        $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$3, $$4.get()));
        if ($$3 != null) {
            $$5.hurtAndBreak(1, (LivingEntity)$$3, $$0.getHand().asEquipmentSlot());
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean playerHasBlockingItemUseIntent(UseOnContext $$0) {
        Player $$1 = $$0.getPlayer();
        return $$0.getHand().equals((Object)InteractionHand.MAIN_HAND) && $$1.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !$$1.isSecondaryUseActive();
    }

    private Optional<BlockState> evaluateNewBlockState(Level $$0, BlockPos $$12, @Nullable Player $$2, BlockState $$3) {
        Optional<BlockState> $$4 = this.getStripped($$3);
        if ($$4.isPresent()) {
            $$0.playSound((Entity)$$2, $$12, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            return $$4;
        }
        Optional<BlockState> $$5 = WeatheringCopper.getPrevious($$3);
        if ($$5.isPresent()) {
            AxeItem.spawnSoundAndParticle($$0, $$12, $$2, $$3, SoundEvents.AXE_SCRAPE, 3005);
            return $$5;
        }
        Optional<BlockState> $$6 = Optional.ofNullable((Block)HoneycombItem.WAX_OFF_BY_BLOCK.get().get((Object)$$3.getBlock())).map($$1 -> $$1.withPropertiesOf($$3));
        if ($$6.isPresent()) {
            AxeItem.spawnSoundAndParticle($$0, $$12, $$2, $$3, SoundEvents.AXE_WAX_OFF, 3004);
            return $$6;
        }
        return Optional.empty();
    }

    private static void spawnSoundAndParticle(Level $$0, BlockPos $$1, @Nullable Player $$2, BlockState $$3, SoundEvent $$4, int $$5) {
        $$0.playSound((Entity)$$2, $$1, $$4, SoundSource.BLOCKS, 1.0f, 1.0f);
        $$0.levelEvent($$2, $$5, $$1, 0);
        if ($$3.getBlock() instanceof ChestBlock && $$3.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos $$6 = ChestBlock.getConnectedBlockPos($$1, $$3);
            $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$6, GameEvent.Context.of($$2, $$0.getBlockState($$6)));
            $$0.levelEvent($$2, $$5, $$6, 0);
        }
    }

    private Optional<BlockState> getStripped(BlockState $$0) {
        return Optional.ofNullable(STRIPPABLES.get($$0.getBlock())).map($$1 -> (BlockState)$$1.defaultBlockState().setValue(RotatedPillarBlock.AXIS, $$0.getValue(RotatedPillarBlock.AXIS)));
    }
}

