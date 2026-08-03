package net.minecraft.world.item.equipment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

public enum ArmorType implements StringRepresentable {
   HELMET(EquipmentSlot.HEAD, 11, "helmet"),
   CHESTPLATE(EquipmentSlot.CHEST, 16, "chestplate"),
   LEGGINGS(EquipmentSlot.LEGS, 15, "leggings"),
   BOOTS(EquipmentSlot.FEET, 13, "boots"),
   BODY(EquipmentSlot.BODY, 16, "body");

   public static final Codec<ArmorType> CODEC = StringRepresentable.fromValues(ArmorType::values);
   private final EquipmentSlot slot;
   private final String name;
   private final int unitDurability;

   private ArmorType(final EquipmentSlot $$0, final int $$1, final String $$2) {
      this.slot = $$0;
      this.name = $$2;
      this.unitDurability = $$1;
   }

   public int getDurability(int $$0) {
      return this.unitDurability * $$0;
   }

   public EquipmentSlot getSlot() {
      return this.slot;
   }

   public String getName() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
