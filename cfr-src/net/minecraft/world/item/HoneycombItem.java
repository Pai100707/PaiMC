/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoneycombItem
extends Item
implements SignApplicator {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.WAXED_COPPER_BLOCK).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WAXED_EXPOSED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.WAXED_WEATHERED_COPPER).put((Object)Blocks.OXIDIZED_COPPER, (Object)Blocks.WAXED_OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.WAXED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER).put((Object)Blocks.OXIDIZED_CUT_COPPER, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.WAXED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.OXIDIZED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.WAXED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS).put((Object)Blocks.CHISELED_COPPER, (Object)Blocks.WAXED_CHISELED_COPPER).put((Object)Blocks.EXPOSED_CHISELED_COPPER, (Object)Blocks.WAXED_EXPOSED_CHISELED_COPPER).put((Object)Blocks.WEATHERED_CHISELED_COPPER, (Object)Blocks.WAXED_WEATHERED_CHISELED_COPPER).put((Object)Blocks.OXIDIZED_CHISELED_COPPER, (Object)Blocks.WAXED_OXIDIZED_CHISELED_COPPER).put((Object)Blocks.COPPER_DOOR, (Object)Blocks.WAXED_COPPER_DOOR).put((Object)Blocks.EXPOSED_COPPER_DOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_DOOR).put((Object)Blocks.WEATHERED_COPPER_DOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_DOOR).put((Object)Blocks.OXIDIZED_COPPER_DOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_DOOR).put((Object)Blocks.COPPER_TRAPDOOR, (Object)Blocks.WAXED_COPPER_TRAPDOOR).put((Object)Blocks.EXPOSED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR).put((Object)Blocks.WEATHERED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR).put((Object)Blocks.OXIDIZED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR).putAll(Blocks.COPPER_BARS.waxedMapping()).put((Object)Blocks.COPPER_GRATE, (Object)Blocks.WAXED_COPPER_GRATE).put((Object)Blocks.EXPOSED_COPPER_GRATE, (Object)Blocks.WAXED_EXPOSED_COPPER_GRATE).put((Object)Blocks.WEATHERED_COPPER_GRATE, (Object)Blocks.WAXED_WEATHERED_COPPER_GRATE).put((Object)Blocks.OXIDIZED_COPPER_GRATE, (Object)Blocks.WAXED_OXIDIZED_COPPER_GRATE).put((Object)Blocks.COPPER_BULB, (Object)Blocks.WAXED_COPPER_BULB).put((Object)Blocks.EXPOSED_COPPER_BULB, (Object)Blocks.WAXED_EXPOSED_COPPER_BULB).put((Object)Blocks.WEATHERED_COPPER_BULB, (Object)Blocks.WAXED_WEATHERED_COPPER_BULB).put((Object)Blocks.OXIDIZED_COPPER_BULB, (Object)Blocks.WAXED_OXIDIZED_COPPER_BULB).put((Object)Blocks.COPPER_CHEST, (Object)Blocks.WAXED_COPPER_CHEST).put((Object)Blocks.EXPOSED_COPPER_CHEST, (Object)Blocks.WAXED_EXPOSED_COPPER_CHEST).put((Object)Blocks.WEATHERED_COPPER_CHEST, (Object)Blocks.WAXED_WEATHERED_COPPER_CHEST).put((Object)Blocks.OXIDIZED_COPPER_CHEST, (Object)Blocks.WAXED_OXIDIZED_COPPER_CHEST).put((Object)Blocks.COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_COPPER_GOLEM_STATUE).put((Object)Blocks.EXPOSED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE).put((Object)Blocks.WEATHERED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE).put((Object)Blocks.OXIDIZED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE).put((Object)Blocks.LIGHTNING_ROD, (Object)Blocks.WAXED_LIGHTNING_ROD).put((Object)Blocks.EXPOSED_LIGHTNING_ROD, (Object)Blocks.WAXED_EXPOSED_LIGHTNING_ROD).put((Object)Blocks.WEATHERED_LIGHTNING_ROD, (Object)Blocks.WAXED_WEATHERED_LIGHTNING_ROD).put((Object)Blocks.OXIDIZED_LIGHTNING_ROD, (Object)Blocks.WAXED_OXIDIZED_LIGHTNING_ROD).putAll(Blocks.COPPER_LANTERN.waxedMapping()).putAll(Blocks.COPPER_CHAIN.waxedMapping()).build());
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());
    private static final String WAXED_COPPER_DOOR = "waxed_copper_door";
    private static final String WAXED_COPPER_TRAPDOOR = "waxed_copper_trapdoor";
    private static final String WAXED_COPPER_GOLEM_STATUE = "waxed_copper_golem_statue";
    private static final String WAXED_COPPER_CHEST = "waxed_copper_chest";
    private static final String WAXED_LIGHTNING_ROD = "waxed_lightning_rod";
    private static final String WAXED_COPPER_BAR = "waxed_copper_bar";
    private static final String WAXED_COPPER_CHAIN = "waxed_copper_chain";
    private static final String WAXED_COPPER_LANTERN = "waxed_copper_lantern";
    private static final String WAXED_COPPER_BLOCK = "waxed_copper_block";
    public static final ImmutableMap<Block, Pair<RecipeCategory, String>> WAXED_RECIPES = ImmutableMap.builder().put((Object)Blocks.WAXED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_bulb")).put((Object)Blocks.WAXED_WEATHERED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_weathered_copper_bulb")).put((Object)Blocks.WAXED_EXPOSED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_exposed_copper_bulb")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_oxidized_copper_bulb")).put((Object)Blocks.WAXED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_WEATHERED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_EXPOSED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_WEATHERED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_EXPOSED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_WEATHERED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_EXPOSED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_OXIDIZED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.COPPER_BARS.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_CHAIN.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAIN.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAIN.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAIN.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_LANTERN.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERN.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERN.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERN.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.WAXED_COPPER_BLOCK, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_WEATHERED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_EXPOSED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_OXIDIZED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).build();

    public HoneycombItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        Level $$1 = $$0.getLevel();
        BlockPos $$2 = $$0.getClickedPos();
        BlockState $$3 = $$1.getBlockState($$2);
        return HoneycombItem.getWaxed($$3).map($$4 -> {
            Player $$5 = $$0.getPlayer();
            ItemStack $$6 = $$0.getItemInHand();
            if ($$5 instanceof ServerPlayer) {
                ServerPlayer $$7 = (ServerPlayer)$$5;
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger($$7, $$2, $$6);
            }
            $$6.shrink(1);
            $$1.setBlock($$2, (BlockState)$$4, 11);
            $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$5, $$4));
            $$1.levelEvent($$5, 3003, $$2, 0);
            if ($$3.getBlock() instanceof ChestBlock && $$3.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos $$8 = ChestBlock.getConnectedBlockPos($$2, $$3);
                $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$8, GameEvent.Context.of($$5, $$1.getBlockState($$8)));
                $$1.levelEvent($$5, 3003, $$8, 0);
            }
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    public static Optional<BlockState> getWaxed(BlockState $$0) {
        return Optional.ofNullable((Block)WAXABLES.get().get((Object)$$0.getBlock())).map($$1 -> $$1.withPropertiesOf($$0));
    }

    @Override
    public boolean tryApplyToSign(Level $$0, SignBlockEntity $$1, boolean $$2, Player $$3) {
        if ($$1.setWaxed(true)) {
            $$0.levelEvent(null, 3003, $$1.getBlockPos(), 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canApplyToSign(SignText $$0, Player $$1) {
        return true;
    }
}

