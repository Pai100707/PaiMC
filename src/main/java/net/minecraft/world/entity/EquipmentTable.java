package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record EquipmentTable(ResourceKey<LootTable> lootTable, Map<net.minecraft.world.entity.EquipmentSlot, Float> slotDropChances) {
   public static final Codec<Map<net.minecraft.world.entity.EquipmentSlot, Float>> DROP_CHANCES_CODEC = Codec.either(
         Codec.FLOAT, Codec.unboundedMap(net.minecraft.world.entity.EquipmentSlot.CODEC, Codec.FLOAT)
      )
      .xmap($$0 -> (Map)$$0.map(net.minecraft.world.entity.EquipmentTable::createForAllSlots, Function.identity()), $$0 -> {
         boolean $$1 = $$0.values().stream().distinct().count() == 1L;
         boolean $$2 = $$0.keySet().containsAll(net.minecraft.world.entity.EquipmentSlot.VALUES);
         return $$1 && $$2 ? Either.left($$0.values().stream().findFirst().orElse(0.0F)) : Either.right($$0);
      });
   public static final Codec<net.minecraft.world.entity.EquipmentTable> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(net.minecraft.world.entity.EquipmentTable::lootTable),
            DROP_CHANCES_CODEC.optionalFieldOf("slot_drop_chances", Map.of()).forGetter(net.minecraft.world.entity.EquipmentTable::slotDropChances)
         )
         .apply($$0, net.minecraft.world.entity.EquipmentTable::new)
   );

   public EquipmentTable(ResourceKey<LootTable> $$0, float $$1) {
      this($$0, createForAllSlots($$1));
   }

   private static Map<net.minecraft.world.entity.EquipmentSlot, Float> createForAllSlots(float $$0) {
      return createForAllSlots(List.of(net.minecraft.world.entity.EquipmentSlot.values()), $$0);
   }

   private static Map<net.minecraft.world.entity.EquipmentSlot, Float> createForAllSlots(List<net.minecraft.world.entity.EquipmentSlot> $$0, float $$1) {
      Map<net.minecraft.world.entity.EquipmentSlot, Float> $$2 = Maps.newHashMap();

      for (net.minecraft.world.entity.EquipmentSlot $$3 : $$0) {
         $$2.put($$3, $$1);
      }

      return $$2;
   }
}
