/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FoodOnAStickItem<T extends Entity>
extends Item {
    private final EntityType<T> canInteractWith;
    private final int consumeItemDamage;

    public FoodOnAStickItem(EntityType<T> $$0, int $$1, Item.Properties $$2) {
        super($$2);
        this.canInteractWith = $$0;
        this.consumeItemDamage = $$1;
    }

    @Override
    public InteractionResult use(Level $$0, Player $$1, InteractionHand $$2) {
        ItemStack $$3 = $$1.getItemInHand($$2);
        if ($$0.isClientSide()) {
            return InteractionResult.PASS;
        }
        Entity $$4 = $$1.getControlledVehicle();
        if ($$1.isPassenger() && $$4 instanceof ItemSteerable) {
            ItemSteerable $$5 = (ItemSteerable)((Object)$$4);
            if ($$4.getType() == this.canInteractWith && $$5.boost()) {
                EquipmentSlot $$6 = $$2.asEquipmentSlot();
                ItemStack $$7 = $$3.hurtAndConvertOnBreak(this.consumeItemDamage, Items.FISHING_ROD, $$1, $$6);
                return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo($$7);
            }
        }
        $$1.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.PASS;
    }
}

