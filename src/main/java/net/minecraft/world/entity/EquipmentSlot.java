package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.StringRepresentable.EnumCodec;
import net.minecraft.world.item.ItemStack;

public enum EquipmentSlot implements StringRepresentable {
   MAINHAND(net.minecraft.world.entity.EquipmentSlot.Type.HAND, 0, 0, "mainhand"),
   OFFHAND(net.minecraft.world.entity.EquipmentSlot.Type.HAND, 1, 5, "offhand"),
   FEET(net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR, 0, 1, 1, "feet"),
   LEGS(net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR, 1, 1, 2, "legs"),
   CHEST(net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR, 2, 1, 3, "chest"),
   HEAD(net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR, 3, 1, 4, "head"),
   BODY(net.minecraft.world.entity.EquipmentSlot.Type.ANIMAL_ARMOR, 0, 1, 6, "body"),
   SADDLE(net.minecraft.world.entity.EquipmentSlot.Type.SADDLE, 0, 1, 7, "saddle");

   public static final int NO_COUNT_LIMIT = 0;
   public static final List<net.minecraft.world.entity.EquipmentSlot> VALUES = List.of(values());
   public static final IntFunction<net.minecraft.world.entity.EquipmentSlot> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final EnumCodec<net.minecraft.world.entity.EquipmentSlot> CODEC = StringRepresentable.fromEnum(
      net.minecraft.world.entity.EquipmentSlot::values
   );
   public static final StreamCodec<ByteBuf, net.minecraft.world.entity.EquipmentSlot> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final net.minecraft.world.entity.EquipmentSlot.Type type;
   private final int index;
   private final int countLimit;
   private final int id;
   private final String name;

   private EquipmentSlot(final net.minecraft.world.entity.EquipmentSlot.Type $$0, final int $$1, final int $$2, final int $$3, final String $$4) {
      this.type = $$0;
      this.index = $$1;
      this.countLimit = $$2;
      this.id = $$3;
      this.name = $$4;
   }

   private EquipmentSlot(final net.minecraft.world.entity.EquipmentSlot.Type $$0, final int $$1, final int $$2, final String $$3) {
      this($$0, $$1, 0, $$2, $$3);
   }

   public net.minecraft.world.entity.EquipmentSlot.Type getType() {
      return this.type;
   }

   public int getIndex() {
      return this.index;
   }

   public int getIndex(int $$0) {
      return $$0 + this.index;
   }

   public ItemStack limit(ItemStack $$0) {
      return this.countLimit > 0 ? $$0.split(this.countLimit) : $$0;
   }

   public int getId() {
      return this.id;
   }

   public int getFilterBit(int $$0) {
      return this.id + $$0;
   }

   public String getName() {
      return this.name;
   }

   public boolean isArmor() {
      return this.type == net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR
         || this.type == net.minecraft.world.entity.EquipmentSlot.Type.ANIMAL_ARMOR;
   }

   public String getSerializedName() {
      return this.name;
   }

   public boolean canIncreaseExperience() {
      return this.type != net.minecraft.world.entity.EquipmentSlot.Type.SADDLE;
   }

   public static net.minecraft.world.entity.EquipmentSlot byName(String $$0) {
      net.minecraft.world.entity.EquipmentSlot $$1 = (net.minecraft.world.entity.EquipmentSlot)CODEC.byName($$0);
      if ($$1 != null) {
         return $$1;
      } else {
         throw new IllegalArgumentException("Invalid slot '" + $$0 + "'");
      }
   }

   public static enum Type {
      HAND,
      HUMANOID_ARMOR,
      ANIMAL_ARMOR,
      SADDLE;
   }
}
