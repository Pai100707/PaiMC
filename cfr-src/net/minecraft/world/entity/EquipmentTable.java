/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<EquipmentSlot, Float> slotDropChances) {
    public static final Codec<Map<EquipmentSlot, Float>> DROP_CHANCES_CODEC = Codec.either((Codec)Codec.FLOAT, (Codec)Codec.unboundedMap(EquipmentSlot.CODEC, (Codec)Codec.FLOAT)).xmap($$0 -> (Map)$$0.map(EquipmentTable::createForAllSlots, Function.identity()), $$0 -> {
        boolean $$1 = $$0.values().stream().distinct().count() == 1L;
        boolean $$2 = $$0.keySet().containsAll(EquipmentSlot.VALUES);
        if ($$1 && $$2) {
            return Either.left((Object)$$0.values().stream().findFirst().orElse(Float.valueOf(0.0f)));
        }
        return Either.right((Object)$$0);
    });
    public static final Codec<EquipmentTable> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(EquipmentTable::lootTable), (App)DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(EquipmentTable::slotDropChances)).apply((Applicative)$$0, EquipmentTable::new));

    public EquipmentTable(ResourceKey<LootTable> $$0, float $$1) {
        this($$0, EquipmentTable.createForAllSlots($$1));
    }

    private static Map<EquipmentSlot, Float> createForAllSlots(float $$0) {
        return EquipmentTable.createForAllSlots(List.of(EquipmentSlot.values()), $$0);
    }

    private static Map<EquipmentSlot, Float> createForAllSlots(List<EquipmentSlot> $$0, float $$1) {
        HashMap $$2 = Maps.newHashMap();
        for (EquipmentSlot $$3 : $$0) {
            $$2.put($$3, Float.valueOf($$1));
        }
        return $$2;
    }
}

