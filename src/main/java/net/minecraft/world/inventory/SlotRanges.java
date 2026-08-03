package net.minecraft.world.inventory;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import org.jspecify.annotations.Nullable;

public class SlotRanges {
   private static final List<net.minecraft.world.inventory.SlotRange> SLOTS = (List<net.minecraft.world.inventory.SlotRange>)Util.make(
      new ArrayList(), $$0 -> {
         addSingleSlot($$0, "contents", 0);
         addSlotRange($$0, "container.", 0, 54);
         addSlotRange($$0, "hotbar.", 0, 9);
         addSlotRange($$0, "inventory.", 9, 27);
         addSlotRange($$0, "enderchest.", 200, 27);
         addSlotRange($$0, "villager.", 300, 8);
         addSlotRange($$0, "horse.", 500, 15);
         int $$1 = EquipmentSlot.MAINHAND.getIndex(98);
         int $$2 = EquipmentSlot.OFFHAND.getIndex(98);
         addSingleSlot($$0, "weapon", $$1);
         addSingleSlot($$0, "weapon.mainhand", $$1);
         addSingleSlot($$0, "weapon.offhand", $$2);
         addSlots($$0, "weapon.*", $$1, $$2);
         $$1 = EquipmentSlot.HEAD.getIndex(100);
         $$2 = EquipmentSlot.CHEST.getIndex(100);
         int $$5 = EquipmentSlot.LEGS.getIndex(100);
         int $$6 = EquipmentSlot.FEET.getIndex(100);
         int $$7 = EquipmentSlot.BODY.getIndex(105);
         addSingleSlot($$0, "armor.head", $$1);
         addSingleSlot($$0, "armor.chest", $$2);
         addSingleSlot($$0, "armor.legs", $$5);
         addSingleSlot($$0, "armor.feet", $$6);
         addSingleSlot($$0, "armor.body", $$7);
         addSlots($$0, "armor.*", $$1, $$2, $$5, $$6, $$7);
         addSingleSlot($$0, "saddle", EquipmentSlot.SADDLE.getIndex(106));
         addSingleSlot($$0, "horse.chest", 499);
         addSingleSlot($$0, "player.cursor", 499);
         addSlotRange($$0, "player.crafting.", 500, 4);
      }
   );
   public static final Codec<net.minecraft.world.inventory.SlotRange> CODEC = StringRepresentable.fromValues(
      () -> SLOTS.toArray(net.minecraft.world.inventory.SlotRange[]::new)
   );
   private static final Function<String, net.minecraft.world.inventory.SlotRange> NAME_LOOKUP = StringRepresentable.createNameLookup(
      SLOTS.toArray(net.minecraft.world.inventory.SlotRange[]::new)
   );

   private static net.minecraft.world.inventory.SlotRange create(String $$0, int $$1) {
      return net.minecraft.world.inventory.SlotRange.of($$0, IntLists.singleton($$1));
   }

   private static net.minecraft.world.inventory.SlotRange create(String $$0, IntList $$1) {
      return net.minecraft.world.inventory.SlotRange.of($$0, IntLists.unmodifiable($$1));
   }

   private static net.minecraft.world.inventory.SlotRange create(String $$0, int... $$1) {
      return net.minecraft.world.inventory.SlotRange.of($$0, IntList.of($$1));
   }

   private static void addSingleSlot(List<net.minecraft.world.inventory.SlotRange> $$0, String $$1, int $$2) {
      $$0.add(create($$1, $$2));
   }

   private static void addSlotRange(List<net.minecraft.world.inventory.SlotRange> $$0, String $$1, int $$2, int $$3) {
      IntList $$4 = new IntArrayList($$3);

      for (int $$5 = 0; $$5 < $$3; $$5++) {
         int $$6 = $$2 + $$5;
         $$0.add(create($$1 + $$5, $$6));
         $$4.add($$6);
      }

      $$0.add(create($$1 + "*", $$4));
   }

   private static void addSlots(List<net.minecraft.world.inventory.SlotRange> $$0, String $$1, int... $$2) {
      $$0.add(create($$1, $$2));
   }

   @Nullable
   public static net.minecraft.world.inventory.SlotRange nameToIds(String $$0) {
      return NAME_LOOKUP.apply($$0);
   }

   public static Stream<String> allNames() {
      return SLOTS.stream().map(StringRepresentable::getSerializedName);
   }

   public static Stream<String> singleSlotNames() {
      return SLOTS.stream().filter($$0 -> $$0.size() == 1).map(StringRepresentable::getSerializedName);
   }
}
