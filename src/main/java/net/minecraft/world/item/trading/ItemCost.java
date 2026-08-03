package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentExactPredicate.Builder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ItemLike;

public record ItemCost(
   Holder<net.minecraft.world.item.Item> item, int count, DataComponentExactPredicate components, net.minecraft.world.item.ItemStack itemStack
) {
   public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            net.minecraft.world.item.Item.CODEC.fieldOf("id").forGetter(ItemCost::item),
            ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemCost::count),
            DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(ItemCost::components)
         )
         .apply($$0, ItemCost::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, ItemCost> STREAM_CODEC = StreamCodec.composite(
      net.minecraft.world.item.Item.STREAM_CODEC,
      ItemCost::item,
      ByteBufCodecs.VAR_INT,
      ItemCost::count,
      DataComponentExactPredicate.STREAM_CODEC,
      ItemCost::components,
      ItemCost::new
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemCost>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs::optional);

   public ItemCost(ItemLike $$0) {
      this($$0, 1);
   }

   public ItemCost(ItemLike $$0, int $$1) {
      this($$0.asItem().builtInRegistryHolder(), $$1, DataComponentExactPredicate.EMPTY);
   }

   public ItemCost(Holder<net.minecraft.world.item.Item> $$0, int $$1, DataComponentExactPredicate $$2) {
      this($$0, $$1, $$2, createStack($$0, $$1, $$2));
   }

   public ItemCost withComponents(UnaryOperator<Builder> $$0) {
      return new ItemCost(this.item, this.count, $$0.apply(DataComponentExactPredicate.builder()).build());
   }

   private static net.minecraft.world.item.ItemStack createStack(Holder<net.minecraft.world.item.Item> $$0, int $$1, DataComponentExactPredicate $$2) {
      return new net.minecraft.world.item.ItemStack($$0, $$1, $$2.asPatch());
   }

   public boolean test(net.minecraft.world.item.ItemStack $$0) {
      return $$0.is(this.item) && this.components.test($$0);
   }
}
