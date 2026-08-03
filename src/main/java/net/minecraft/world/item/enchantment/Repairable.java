package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Repairable(HolderSet<net.minecraft.world.item.Item> items) {
   public static final Codec<Repairable> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(Repairable::items)).apply($$0, Repairable::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Repairable> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.holderSet(Registries.ITEM), Repairable::items, Repairable::new
   );

   public boolean isValidRepairItem(net.minecraft.world.item.ItemStack $$0) {
      return $$0.is(this.items);
   }
}
