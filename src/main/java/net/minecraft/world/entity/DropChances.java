package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public record DropChances(Map<net.minecraft.world.entity.EquipmentSlot, Float> byEquipment) {
   public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
   public static final float PRESERVE_ITEM_DROP_CHANCE_THRESHOLD = 1.0F;
   public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
   public static final net.minecraft.world.entity.DropChances DEFAULT = new net.minecraft.world.entity.DropChances(
      Util.makeEnumMap(net.minecraft.world.entity.EquipmentSlot.class, $$0 -> 0.085F)
   );
   public static final Codec<net.minecraft.world.entity.DropChances> CODEC = Codec.unboundedMap(
         net.minecraft.world.entity.EquipmentSlot.CODEC, ExtraCodecs.NON_NEGATIVE_FLOAT
      )
      .xmap(net.minecraft.world.entity.DropChances::toEnumMap, net.minecraft.world.entity.DropChances::filterDefaultValues)
      .xmap(net.minecraft.world.entity.DropChances::new, net.minecraft.world.entity.DropChances::byEquipment);

   private static Map<net.minecraft.world.entity.EquipmentSlot, Float> filterDefaultValues(Map<net.minecraft.world.entity.EquipmentSlot, Float> $$0) {
      Map<net.minecraft.world.entity.EquipmentSlot, Float> $$1 = new HashMap<>($$0);
      $$1.values().removeIf($$0x -> $$0x == 0.085F);
      return $$1;
   }

   private static Map<net.minecraft.world.entity.EquipmentSlot, Float> toEnumMap(Map<net.minecraft.world.entity.EquipmentSlot, Float> $$0) {
      return Util.makeEnumMap(net.minecraft.world.entity.EquipmentSlot.class, $$1 -> $$0.getOrDefault($$1, 0.085F));
   }

   public net.minecraft.world.entity.DropChances withGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot $$0) {
      return this.withEquipmentChance($$0, 2.0F);
   }

   public net.minecraft.world.entity.DropChances withEquipmentChance(net.minecraft.world.entity.EquipmentSlot $$0, float $$1) {
      if ($$1 < 0.0F) {
         throw new IllegalArgumentException("Tried to set invalid equipment chance " + $$1 + " for " + $$0);
      } else {
         return this.byEquipment($$0) == $$1
            ? this
            : new net.minecraft.world.entity.DropChances(
               Util.makeEnumMap(net.minecraft.world.entity.EquipmentSlot.class, $$2 -> $$2 == $$0 ? $$1 : this.byEquipment($$2))
            );
      }
   }

   public float byEquipment(net.minecraft.world.entity.EquipmentSlot $$0) {
      return this.byEquipment.getOrDefault($$0, 0.085F);
   }

   public boolean isPreserved(net.minecraft.world.entity.EquipmentSlot $$0) {
      return this.byEquipment($$0) > 1.0F;
   }
}
