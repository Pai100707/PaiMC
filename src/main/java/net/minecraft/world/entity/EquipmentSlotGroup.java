package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum EquipmentSlotGroup implements StringRepresentable, Iterable<net.minecraft.world.entity.EquipmentSlot> {
   ANY(0, "any", $$0 -> true),
   MAINHAND(1, "mainhand", net.minecraft.world.entity.EquipmentSlot.MAINHAND),
   OFFHAND(2, "offhand", net.minecraft.world.entity.EquipmentSlot.OFFHAND),
   HAND(3, "hand", $$0 -> $$0.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HAND),
   FEET(4, "feet", net.minecraft.world.entity.EquipmentSlot.FEET),
   LEGS(5, "legs", net.minecraft.world.entity.EquipmentSlot.LEGS),
   CHEST(6, "chest", net.minecraft.world.entity.EquipmentSlot.CHEST),
   HEAD(7, "head", net.minecraft.world.entity.EquipmentSlot.HEAD),
   ARMOR(8, "armor", net.minecraft.world.entity.EquipmentSlot::isArmor),
   BODY(9, "body", net.minecraft.world.entity.EquipmentSlot.BODY),
   SADDLE(10, "saddle", net.minecraft.world.entity.EquipmentSlot.SADDLE);

   public static final IntFunction<net.minecraft.world.entity.EquipmentSlotGroup> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<net.minecraft.world.entity.EquipmentSlotGroup> CODEC = StringRepresentable.fromEnum(
      net.minecraft.world.entity.EquipmentSlotGroup::values
   );
   public static final StreamCodec<ByteBuf, net.minecraft.world.entity.EquipmentSlotGroup> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final String key;
   private final Predicate<net.minecraft.world.entity.EquipmentSlot> predicate;
   private final List<net.minecraft.world.entity.EquipmentSlot> slots;

   private EquipmentSlotGroup(final int $$0, final String $$1, final Predicate<net.minecraft.world.entity.EquipmentSlot> $$2) {
      this.id = $$0;
      this.key = $$1;
      this.predicate = $$2;
      this.slots = net.minecraft.world.entity.EquipmentSlot.VALUES.stream().filter($$2).toList();
   }

   private EquipmentSlotGroup(final int $$0, final String $$1, final net.minecraft.world.entity.EquipmentSlot $$2) {
      this($$0, $$1, $$1x -> $$1x == $$2);
   }

   public static net.minecraft.world.entity.EquipmentSlotGroup bySlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return switch ($$0) {
         case MAINHAND -> MAINHAND;
         case OFFHAND -> OFFHAND;
         case FEET -> FEET;
         case LEGS -> LEGS;
         case CHEST -> CHEST;
         case HEAD -> HEAD;
         case BODY -> BODY;
         case SADDLE -> SADDLE;
      };
   }

   public String getSerializedName() {
      return this.key;
   }

   public boolean test(net.minecraft.world.entity.EquipmentSlot $$0) {
      return this.predicate.test($$0);
   }

   public List<net.minecraft.world.entity.EquipmentSlot> slots() {
      return this.slots;
   }

   @Override
   public Iterator<net.minecraft.world.entity.EquipmentSlot> iterator() {
      return this.slots.iterator();
   }
}
