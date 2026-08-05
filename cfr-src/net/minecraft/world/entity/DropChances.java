/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;

public record DropChances(Map<EquipmentSlot, Float> byEquipment) {
    public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085f;
    public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0f;
    public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
    public static final DropChances DEFAULT = new DropChances(Util.makeEnumMap(EquipmentSlot.class, $$0 -> Float.valueOf(0.085f)));
    public static final Codec<DropChances> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ExtraCodecs.NON_NEGATIVE_FLOAT).xmap(DropChances::toEnumMap, DropChances::filterDefaultValues).xmap(DropChances::new, DropChances::byEquipment);

    private static Map<EquipmentSlot, Float> filterDefaultValues(Map<EquipmentSlot, Float> $$02) {
        HashMap<EquipmentSlot, Float> $$1 = new HashMap<EquipmentSlot, Float>($$02);
        $$1.values().removeIf($$0 -> $$0.floatValue() == 0.085f);
        return $$1;
    }

    private static Map<EquipmentSlot, Float> toEnumMap(Map<EquipmentSlot, Float> $$0) {
        return Util.makeEnumMap(EquipmentSlot.class, $$1 -> $$0.getOrDefault($$1, Float.valueOf(0.085f)));
    }

    public DropChances withGuaranteedDrop(EquipmentSlot $$0) {
        return this.withEquipmentChance($$0, 2.0f);
    }

    public DropChances withEquipmentChance(EquipmentSlot $$0, float $$1) {
        if ($$1 < 0.0f) {
            throw new IllegalArgumentException("Tried to set invalid equipment chance " + $$1 + " for " + String.valueOf($$0));
        }
        if (this.byEquipment($$0) == $$1) {
            return this;
        }
        return new DropChances(Util.makeEnumMap(EquipmentSlot.class, $$2 -> Float.valueOf($$2 == $$0 ? $$1 : this.byEquipment((EquipmentSlot)$$2))));
    }

    public float byEquipment(EquipmentSlot $$0) {
        return this.byEquipment.getOrDefault($$0, Float.valueOf(0.085f)).floatValue();
    }

    public boolean isPreserved(EquipmentSlot $$0) {
        return this.byEquipment($$0) > 1.0f;
    }
}

