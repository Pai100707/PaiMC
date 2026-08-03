package net.minecraft.network;

import com.mojang.datafixers.DataFixUtils;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface HashedStack {
   net.minecraft.network.HashedStack EMPTY = new net.minecraft.network.HashedStack() {
      @Override
      public String toString() {
         return "<empty>";
      }

      @Override
      public boolean matches(ItemStack $$0, net.minecraft.network.HashedPatchMap.HashGenerator $$1) {
         return $$0.isEmpty();
      }
   };
   StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.network.HashedStack> STREAM_CODEC = ByteBufCodecs.optional(
         net.minecraft.network.HashedStack.ActualItem.STREAM_CODEC
      )
      .map(
         $$0 -> (net.minecraft.network.HashedStack)DataFixUtils.orElse($$0, EMPTY),
         $$0 -> $$0 instanceof net.minecraft.network.HashedStack.ActualItem $$1 ? Optional.of($$1) : Optional.empty()
      );

   boolean matches(ItemStack var1, net.minecraft.network.HashedPatchMap.HashGenerator var2);

   static net.minecraft.network.HashedStack create(ItemStack $$0, net.minecraft.network.HashedPatchMap.HashGenerator $$1) {
      return (net.minecraft.network.HashedStack)($$0.isEmpty()
         ? EMPTY
         : new net.minecraft.network.HashedStack.ActualItem(
            $$0.getItemHolder(), $$0.getCount(), net.minecraft.network.HashedPatchMap.create($$0.getComponentsPatch(), $$1)
         ));
   }

   public record ActualItem(Holder<Item> item, int count, net.minecraft.network.HashedPatchMap components) implements net.minecraft.network.HashedStack {
      public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, net.minecraft.network.HashedStack.ActualItem> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.holderRegistry(Registries.ITEM),
         net.minecraft.network.HashedStack.ActualItem::item,
         ByteBufCodecs.VAR_INT,
         net.minecraft.network.HashedStack.ActualItem::count,
         net.minecraft.network.HashedPatchMap.STREAM_CODEC,
         net.minecraft.network.HashedStack.ActualItem::components,
         net.minecraft.network.HashedStack.ActualItem::new
      );

      @Override
      public boolean matches(ItemStack $$0, net.minecraft.network.HashedPatchMap.HashGenerator $$1) {
         if (this.count != $$0.getCount()) {
            return false;
         } else {
            return !this.item.equals($$0.getItemHolder()) ? false : this.components.matches($$0.getComponentsPatch(), $$1);
         }
      }
   }
}
