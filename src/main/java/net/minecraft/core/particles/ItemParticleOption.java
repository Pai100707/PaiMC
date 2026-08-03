package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemParticleOption implements ParticleOptions {
   private static final Codec<ItemStack> ITEM_CODEC = Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC, Item.CODEC, ItemStack::new);
   private final ParticleType<ItemParticleOption> type;
   private final ItemStack itemStack;

   public static MapCodec<ItemParticleOption> codec(ParticleType<ItemParticleOption> $$0) {
      return ITEM_CODEC.xmap($$1 -> new ItemParticleOption($$0, $$1), $$0x -> $$0x.itemStack).fieldOf("item");
   }

   public static StreamCodec<? super RegistryFriendlyByteBuf, ItemParticleOption> streamCodec(ParticleType<ItemParticleOption> $$0) {
      return ItemStack.STREAM_CODEC.map($$1 -> new ItemParticleOption($$0, $$1), $$0x -> $$0x.itemStack);
   }

   public ItemParticleOption(ParticleType<ItemParticleOption> $$0, ItemStack $$1) {
      if ($$1.isEmpty()) {
         throw new IllegalArgumentException("Empty stacks are not allowed");
      } else {
         this.type = $$0;
         this.itemStack = $$1;
      }
   }

   @Override
   public ParticleType<ItemParticleOption> getType() {
      return this.type;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }
}
