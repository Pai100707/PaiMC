/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

public interface CauldronInteraction {
    public static final Map<String, InteractionMap> INTERACTIONS = new Object2ObjectArrayMap();
    public static final Codec<InteractionMap> CODEC = Codec.stringResolver(InteractionMap::name, INTERACTIONS::get);
    public static final InteractionMap EMPTY = CauldronInteraction.newInteractionMap("empty");
    public static final InteractionMap WATER = CauldronInteraction.newInteractionMap("water");
    public static final InteractionMap LAVA = CauldronInteraction.newInteractionMap("lava");
    public static final InteractionMap POWDER_SNOW = CauldronInteraction.newInteractionMap("powder_snow");

    public static InteractionMap newInteractionMap(String $$02) {
        Object2ObjectOpenHashMap $$12 = new Object2ObjectOpenHashMap();
        $$12.defaultReturnValue(($$0, $$1, $$2, $$3, $$4, $$5) -> InteractionResult.TRY_WITH_EMPTY_HAND);
        InteractionMap $$22 = new InteractionMap($$02, (Map<Item, CauldronInteraction>)$$12);
        INTERACTIONS.put($$02, $$22);
        return $$22;
    }

    public InteractionResult interact(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, ItemStack var6);

    public static void bootStrap() {
        Map<Item, CauldronInteraction> $$03 = EMPTY.map();
        CauldronInteraction.addDefaultInteractions($$03);
        $$03.put(Items.POTION, ($$0, $$1, $$2, $$3, $$4, $$5) -> {
            PotionContents $$6 = $$5.get(DataComponents.POTION_CONTENTS);
            if ($$6 == null || !$$6.is(Potions.WATER)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!$$1.isClientSide()) {
                Item $$7 = $$5.getItem();
                $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, new ItemStack(Items.GLASS_BOTTLE)));
                $$3.awardStat(Stats.USE_CAULDRON);
                $$3.awardStat(Stats.ITEM_USED.get($$7));
                $$1.setBlockAndUpdate($$2, Blocks.WATER_CAULDRON.defaultBlockState());
                $$1.playSound(null, $$2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                $$1.gameEvent(null, GameEvent.FLUID_PLACE, $$2);
            }
            return InteractionResult.SUCCESS;
        });
        Map<Item, CauldronInteraction> $$12 = WATER.map();
        CauldronInteraction.addDefaultInteractions($$12);
        $$12.put(Items.BUCKET, ($$02, $$1, $$2, $$3, $$4, $$5) -> CauldronInteraction.fillBucket($$02, $$1, $$2, $$3, $$4, $$5, new ItemStack(Items.WATER_BUCKET), $$0 -> $$0.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        $$12.put(Items.GLASS_BOTTLE, ($$0, $$1, $$2, $$3, $$4, $$5) -> {
            if (!$$1.isClientSide()) {
                Item $$6 = $$5.getItem();
                $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                $$3.awardStat(Stats.USE_CAULDRON);
                $$3.awardStat(Stats.ITEM_USED.get($$6));
                LayeredCauldronBlock.lowerFillLevel($$0, $$1, $$2);
                $$1.playSound(null, $$2, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                $$1.gameEvent(null, GameEvent.FLUID_PICKUP, $$2);
            }
            return InteractionResult.SUCCESS;
        });
        $$12.put(Items.POTION, ($$0, $$1, $$2, $$3, $$4, $$5) -> {
            if ($$0.getValue(LayeredCauldronBlock.LEVEL) == 3) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            PotionContents $$6 = $$5.get(DataComponents.POTION_CONTENTS);
            if ($$6 == null || !$$6.is(Potions.WATER)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!$$1.isClientSide()) {
                $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, new ItemStack(Items.GLASS_BOTTLE)));
                $$3.awardStat(Stats.USE_CAULDRON);
                $$3.awardStat(Stats.ITEM_USED.get($$5.getItem()));
                $$1.setBlockAndUpdate($$2, (BlockState)$$0.cycle(LayeredCauldronBlock.LEVEL));
                $$1.playSound(null, $$2, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                $$1.gameEvent(null, GameEvent.FLUID_PLACE, $$2);
            }
            return InteractionResult.SUCCESS;
        });
        $$12.put(Items.LEATHER_BOOTS, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.LEATHER_LEGGINGS, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.LEATHER_CHESTPLATE, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.LEATHER_HELMET, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.LEATHER_HORSE_ARMOR, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.WOLF_ARMOR, CauldronInteraction::dyedItemIteration);
        $$12.put(Items.WHITE_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.GRAY_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.BLACK_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.BLUE_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.BROWN_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.CYAN_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.GREEN_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.LIGHT_BLUE_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.LIGHT_GRAY_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.LIME_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.MAGENTA_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.ORANGE_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.PINK_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.PURPLE_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.RED_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.YELLOW_BANNER, CauldronInteraction::bannerInteraction);
        $$12.put(Items.WHITE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.BLACK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.BROWN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.CYAN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.GREEN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.LIGHT_BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.LIGHT_GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.LIME_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.MAGENTA_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.ORANGE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.PINK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.PURPLE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.RED_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        $$12.put(Items.YELLOW_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        Map<Item, CauldronInteraction> $$22 = LAVA.map();
        $$22.put(Items.BUCKET, ($$02, $$1, $$2, $$3, $$4, $$5) -> CauldronInteraction.fillBucket($$02, $$1, $$2, $$3, $$4, $$5, new ItemStack(Items.LAVA_BUCKET), $$0 -> true, SoundEvents.BUCKET_FILL_LAVA));
        CauldronInteraction.addDefaultInteractions($$22);
        Map<Item, CauldronInteraction> $$32 = POWDER_SNOW.map();
        $$32.put(Items.BUCKET, ($$02, $$1, $$2, $$3, $$4, $$5) -> CauldronInteraction.fillBucket($$02, $$1, $$2, $$3, $$4, $$5, new ItemStack(Items.POWDER_SNOW_BUCKET), $$0 -> $$0.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
        CauldronInteraction.addDefaultInteractions($$32);
    }

    public static void addDefaultInteractions(Map<Item, CauldronInteraction> $$0) {
        $$0.put(Items.LAVA_BUCKET, CauldronInteraction::fillLavaInteraction);
        $$0.put(Items.WATER_BUCKET, CauldronInteraction::fillWaterInteraction);
        $$0.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction::fillPowderSnowInteraction);
    }

    public static InteractionResult fillBucket(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5, ItemStack $$6, Predicate<BlockState> $$7, SoundEvent $$8) {
        if (!$$7.test($$0)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!$$1.isClientSide()) {
            Item $$9 = $$5.getItem();
            $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, $$6));
            $$3.awardStat(Stats.USE_CAULDRON);
            $$3.awardStat(Stats.ITEM_USED.get($$9));
            $$1.setBlockAndUpdate($$2, Blocks.CAULDRON.defaultBlockState());
            $$1.playSound(null, $$2, $$8, SoundSource.BLOCKS, 1.0f, 1.0f);
            $$1.gameEvent(null, GameEvent.FLUID_PICKUP, $$2);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult emptyBucket(Level $$0, BlockPos $$1, Player $$2, InteractionHand $$3, ItemStack $$4, BlockState $$5, SoundEvent $$6) {
        if (!$$0.isClientSide()) {
            Item $$7 = $$4.getItem();
            $$2.setItemInHand($$3, ItemUtils.createFilledResult($$4, $$2, new ItemStack(Items.BUCKET)));
            $$2.awardStat(Stats.FILL_CAULDRON);
            $$2.awardStat(Stats.ITEM_USED.get($$7));
            $$0.setBlockAndUpdate($$1, $$5);
            $$0.playSound(null, $$1, $$6, SoundSource.BLOCKS, 1.0f, 1.0f);
            $$0.gameEvent(null, GameEvent.FLUID_PLACE, $$1);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fillWaterInteraction(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        return CauldronInteraction.emptyBucket($$1, $$2, $$3, $$4, $$5, (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
    }

    private static InteractionResult fillLavaInteraction(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        return CauldronInteraction.isUnderWater($$1, $$2) ? InteractionResult.CONSUME : CauldronInteraction.emptyBucket($$1, $$2, $$3, $$4, $$5, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
    }

    private static InteractionResult fillPowderSnowInteraction(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        return CauldronInteraction.isUnderWater($$1, $$2) ? InteractionResult.CONSUME : CauldronInteraction.emptyBucket($$1, $$2, $$3, $$4, $$5, (BlockState)Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    }

    private static InteractionResult shulkerBoxInteraction(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        Block $$6 = Block.byItem($$5.getItem());
        if (!($$6 instanceof ShulkerBoxBlock)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!$$1.isClientSide()) {
            ItemStack $$7 = $$5.transmuteCopy(Blocks.SHULKER_BOX, 1);
            $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, $$7, false));
            $$3.awardStat(Stats.CLEAN_SHULKER_BOX);
            LayeredCauldronBlock.lowerFillLevel($$0, $$1, $$2);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult bannerInteraction(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        BannerPatternLayers $$6 = $$5.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if ($$6.layers().isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!$$1.isClientSide()) {
            ItemStack $$7 = $$5.copyWithCount(1);
            $$7.set(DataComponents.BANNER_PATTERNS, $$6.removeLast());
            $$3.setItemInHand($$4, ItemUtils.createFilledResult($$5, $$3, $$7, false));
            $$3.awardStat(Stats.CLEAN_BANNER);
            LayeredCauldronBlock.lowerFillLevel($$0, $$1, $$2);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult dyedItemIteration(BlockState $$0, Level $$1, BlockPos $$2, Player $$3, InteractionHand $$4, ItemStack $$5) {
        if (!$$5.is(ItemTags.DYEABLE)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!$$5.has(DataComponents.DYED_COLOR)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!$$1.isClientSide()) {
            $$5.remove(DataComponents.DYED_COLOR);
            $$3.awardStat(Stats.CLEAN_ARMOR);
            LayeredCauldronBlock.lowerFillLevel($$0, $$1, $$2);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isUnderWater(Level $$0, BlockPos $$1) {
        FluidState $$2 = $$0.getFluidState($$1.above());
        return $$2.is(FluidTags.WATER);
    }

    public record InteractionMap(String name, Map<Item, CauldronInteraction> map) {
    }
}

