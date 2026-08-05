/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class DiscFragmentItem
extends Item {
    public DiscFragmentItem(Item.Properties $$0) {
        super($$0);
    }

    @Override
    public void appendHoverText(ItemStack $$0, Item.TooltipContext $$1, TooltipDisplay $$2, Consumer<Component> $$3, TooltipFlag $$4) {
        $$3.accept(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    public MutableComponent getDisplayName() {
        return Component.translatable(this.descriptionId + ".desc");
    }
}

