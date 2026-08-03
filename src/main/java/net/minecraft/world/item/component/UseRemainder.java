package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record UseRemainder(net.minecraft.world.item.ItemStack convertInto) {
   public static final Codec<UseRemainder> CODEC = net.minecraft.world.item.ItemStack.CODEC.xmap(UseRemainder::new, UseRemainder::convertInto);
   public static final StreamCodec<RegistryFriendlyByteBuf, UseRemainder> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.item.ItemStack.STREAM_CODEC, UseRemainder::convertInto, UseRemainder::new
   );

   public net.minecraft.world.item.ItemStack convertIntoRemainder(
      net.minecraft.world.item.ItemStack $$0, int $$1, boolean $$2, UseRemainder.OnExtraCreatedRemainder $$3
   ) {
      if ($$2) {
         return $$0;
      } else if ($$0.getCount() >= $$1) {
         return $$0;
      } else {
         net.minecraft.world.item.ItemStack $$4 = this.convertInto.copy();
         if ($$0.isEmpty()) {
            return $$4;
         } else {
            $$3.apply($$4);
            return $$0;
         }
      }
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         UseRemainder $$1 = (UseRemainder)$$0;
         return net.minecraft.world.item.ItemStack.matches(this.convertInto, $$1.convertInto);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return net.minecraft.world.item.ItemStack.hashItemAndComponents(this.convertInto);
   }

   @FunctionalInterface
   public interface OnExtraCreatedRemainder {
      void apply(net.minecraft.world.item.ItemStack var1);
   }
}
