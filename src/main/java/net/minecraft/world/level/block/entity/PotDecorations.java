package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipProvider;

public record PotDecorations(Optional<Item> back, Optional<Item> left, Optional<Item> right, Optional<Item> front) implements TooltipProvider {
   public static final PotDecorations EMPTY = new PotDecorations(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
   public static final Codec<PotDecorations> CODEC = BuiltInRegistries.ITEM
      .byNameCodec()
      .sizeLimitedListOf(4)
      .xmap(PotDecorations::new, PotDecorations::ordered);
   public static final StreamCodec<RegistryFriendlyByteBuf, PotDecorations> STREAM_CODEC = ByteBufCodecs.registry(Registries.ITEM)
      .apply(ByteBufCodecs.list(4))
      .map(PotDecorations::new, PotDecorations::ordered);

   private PotDecorations(List<Item> $$0) {
      this(getItem($$0, 0), getItem($$0, 1), getItem($$0, 2), getItem($$0, 3));
   }

   public PotDecorations(Item $$0, Item $$1, Item $$2, Item $$3) {
      this(List.of($$0, $$1, $$2, $$3));
   }

   private static Optional<Item> getItem(List<Item> $$0, int $$1) {
      if ($$1 >= $$0.size()) {
         return Optional.empty();
      } else {
         Item $$2 = $$0.get($$1);
         return $$2 == Items.BRICK ? Optional.empty() : Optional.of($$2);
      }
   }

   public List<Item> ordered() {
      return Stream.of(this.back, this.left, this.right, this.front).map($$0 -> $$0.orElse(Items.BRICK)).toList();
   }

   public void addToTooltip(TooltipContext $$0, Consumer<Component> $$1, TooltipFlag $$2, DataComponentGetter $$3) {
      if (!this.equals(EMPTY)) {
         $$1.accept(CommonComponents.EMPTY);
         addSideDetailsToTooltip($$1, this.front);
         addSideDetailsToTooltip($$1, this.left);
         addSideDetailsToTooltip($$1, this.right);
         addSideDetailsToTooltip($$1, this.back);
      }
   }

   private static void addSideDetailsToTooltip(Consumer<Component> $$0, Optional<Item> $$1) {
      $$0.accept(new ItemStack((net.minecraft.world.level.ItemLike)$$1.orElse(Items.BRICK), 1).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY));
   }
}
