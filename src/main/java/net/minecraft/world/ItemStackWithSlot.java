package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public record ItemStackWithSlot(int slot, ItemStack stack) {
   public static final Codec<ItemStackWithSlot> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.UNSIGNED_BYTE.fieldOf("Slot").orElse(0).forGetter(ItemStackWithSlot::slot), ItemStack.MAP_CODEC.forGetter(ItemStackWithSlot::stack)
         )
         .apply($$0, ItemStackWithSlot::new)
   );

   public boolean isValidInContainer(int $$0) {
      return this.slot >= 0 && this.slot < $$0;
   }
}
