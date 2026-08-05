/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoeItem
extends Item {
    protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap((Map)ImmutableMap.of((Object)Blocks.GRASS_BLOCK, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.DIRT_PATH, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.DIRT, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())), (Object)Blocks.COARSE_DIRT, (Object)Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.DIRT.defaultBlockState())), (Object)Blocks.ROOTED_DIRT, (Object)Pair.of($$0 -> true, HoeItem.changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))));

    public HoeItem(ToolMaterial $$0, float $$1, float $$2, Item.Properties $$3) {
        super($$3.hoe($$0, $$1, $$2));
    }

    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        BlockPos $$2;
        Level $$1 = $$0.getLevel();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> $$3 = TILLABLES.get($$1.getBlockState($$2 = $$0.getClickedPos()).getBlock());
        if ($$3 == null) {
            return InteractionResult.PASS;
        }
        Predicate $$4 = (Predicate)$$3.getFirst();
        Consumer $$5 = (Consumer)$$3.getSecond();
        if ($$4.test($$0)) {
            Player $$6 = $$0.getPlayer();
            $$1.playSound((Entity)$$6, $$2, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!$$1.isClientSide()) {
                $$5.accept($$0);
                if ($$6 != null) {
                    $$0.getItemInHand().hurtAndBreak(1, (LivingEntity)$$6, $$0.getHand().asEquipmentSlot());
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState $$0) {
        return $$1 -> {
            $$1.getLevel().setBlock($$1.getClickedPos(), $$0, 11);
            $$1.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, $$1.getClickedPos(), GameEvent.Context.of($$1.getPlayer(), $$0));
        };
    }

    public static Consumer<UseOnContext> changeIntoStateAndDropItem(BlockState $$0, ItemLike $$1) {
        return $$2 -> {
            $$2.getLevel().setBlock($$2.getClickedPos(), $$0, 11);
            $$2.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, $$2.getClickedPos(), GameEvent.Context.of($$2.getPlayer(), $$0));
            Block.popResourceFromFace($$2.getLevel(), $$2.getClickedPos(), $$2.getClickedFace(), new ItemStack($$1));
        };
    }

    public static boolean onlyIfAirAbove(UseOnContext $$0) {
        return $$0.getClickedFace() != Direction.DOWN && $$0.getLevel().getBlockState($$0.getClickedPos().above()).isAir();
    }
}

