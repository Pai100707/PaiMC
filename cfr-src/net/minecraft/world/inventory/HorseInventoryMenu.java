/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;

public class HorseInventoryMenu
extends AbstractMountInventoryMenu {
    private static final Identifier SADDLE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/saddle");
    private static final Identifier LLAMA_ARMOR_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/llama_armor");
    private static final Identifier ARMOR_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/horse_armor");

    public HorseInventoryMenu(int $$0, Inventory $$1, Container $$2, final AbstractHorse $$3, int $$4) {
        super($$0, $$1, $$2, $$3);
        Container $$5 = $$3.createEquipmentSlotContainer(EquipmentSlot.SADDLE);
        this.addSlot(new ArmorSlot(this, $$5, $$3, EquipmentSlot.SADDLE, 0, 8, 18, SADDLE_SLOT_SPRITE){

            @Override
            public boolean isActive() {
                return $$3.canUseSlot(EquipmentSlot.SADDLE) && $$3.getType().is(EntityTypeTags.CAN_EQUIP_SADDLE);
            }
        });
        final boolean $$6 = $$3 instanceof Llama;
        Identifier $$7 = $$6 ? LLAMA_ARMOR_SLOT_SPRITE : ARMOR_SLOT_SPRITE;
        Container $$8 = $$3.createEquipmentSlotContainer(EquipmentSlot.BODY);
        this.addSlot(new ArmorSlot(this, $$8, $$3, EquipmentSlot.BODY, 0, 8, 36, $$7){

            @Override
            public boolean isActive() {
                return $$3.canUseSlot(EquipmentSlot.BODY) && ($$3.getType().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || $$6);
            }
        });
        if ($$4 > 0) {
            for (int $$9 = 0; $$9 < 3; ++$$9) {
                for (int $$10 = 0; $$10 < $$4; ++$$10) {
                    this.addSlot(new Slot($$2, $$10 + $$9 * $$4, 80 + $$10 * 18, 18 + $$9 * 18));
                }
            }
        }
        this.addStandardInventorySlots($$1, 8, 84);
    }

    @Override
    protected boolean hasInventoryChanged(Container $$0) {
        return ((AbstractHorse)this.mount).hasInventoryChanged($$0);
    }
}

