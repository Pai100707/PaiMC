/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem
extends Item {
    private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> $$0, Item.Properties $$1) {
        super($$1);
        this.type = $$0;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public InteractionResult useOn(UseOnContext $$0) {
        void $$11;
        BlockPos $$1 = $$0.getClickedPos();
        Direction $$2 = $$0.getClickedFace();
        BlockPos $$3 = $$1.relative($$2);
        Player $$4 = $$0.getPlayer();
        ItemStack $$5 = $$0.getItemInHand();
        if ($$4 != null && !this.mayPlace($$4, $$2, $$5, $$3)) {
            return InteractionResult.FAIL;
        }
        Level $$6 = $$0.getLevel();
        if (this.type == EntityType.PAINTING) {
            Optional<Painting> $$7 = Painting.create($$6, $$3, $$2);
            if ($$7.isEmpty()) {
                return InteractionResult.CONSUME;
            }
            HangingEntity $$8 = $$7.get();
        } else if (this.type == EntityType.ITEM_FRAME) {
            ItemFrame $$9 = new ItemFrame($$6, $$3, $$2);
        } else if (this.type == EntityType.GLOW_ITEM_FRAME) {
            GlowItemFrame $$10 = new GlowItemFrame($$6, $$3, $$2);
        } else {
            return InteractionResult.SUCCESS;
        }
        EntityType.createDefaultStackConfig($$6, $$5, $$4).accept($$11);
        if ($$11.survives()) {
            if (!$$6.isClientSide()) {
                $$11.playPlacementSound();
                $$6.gameEvent((Entity)$$4, GameEvent.ENTITY_PLACE, $$11.position());
                $$6.addFreshEntity((Entity)$$11);
            }
            $$5.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    protected boolean mayPlace(Player $$0, Direction $$1, ItemStack $$2, BlockPos $$3) {
        return !$$1.getAxis().isVertical() && $$0.mayUseItemAt($$3, $$1, $$2);
    }

    @Override
    public void appendHoverText(ItemStack $$0, Item.TooltipContext $$1, TooltipDisplay $$2, Consumer<Component> $$3, TooltipFlag $$4) {
        if (this.type == EntityType.PAINTING && $$2.shows(DataComponents.PAINTING_VARIANT)) {
            Holder<PaintingVariant> $$5 = $$0.get(DataComponents.PAINTING_VARIANT);
            if ($$5 != null) {
                $$5.value().title().ifPresent($$3);
                $$5.value().author().ifPresent($$3);
                $$3.accept(Component.translatable("painting.dimensions", $$5.value().width(), $$5.value().height()));
            } else if ($$4.isCreative()) {
                $$3.accept(TOOLTIP_RANDOM_VARIANT);
            }
        }
    }
}

