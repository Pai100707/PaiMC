package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ChargedProjectiles implements TooltipProvider {
   public static final ChargedProjectiles EMPTY = new ChargedProjectiles(List.of());
   public static final Codec<ChargedProjectiles> CODEC = net.minecraft.world.item.ItemStack.CODEC.listOf().xmap(ChargedProjectiles::new, $$0 -> $$0.items);
   public static final StreamCodec<RegistryFriendlyByteBuf, ChargedProjectiles> STREAM_CODEC = net.minecraft.world.item.ItemStack.STREAM_CODEC
      .apply(ByteBufCodecs.list())
      .map(ChargedProjectiles::new, $$0 -> $$0.items);
   private final List<net.minecraft.world.item.ItemStack> items;

   private ChargedProjectiles(List<net.minecraft.world.item.ItemStack> $$0) {
      this.items = $$0;
   }

   public static ChargedProjectiles of(net.minecraft.world.item.ItemStack $$0) {
      return new ChargedProjectiles(List.of($$0.copy()));
   }

   public static ChargedProjectiles of(List<net.minecraft.world.item.ItemStack> $$0) {
      return new ChargedProjectiles(List.copyOf(Lists.transform($$0, net.minecraft.world.item.ItemStack::copy)));
   }

   public boolean contains(net.minecraft.world.item.Item $$0) {
      for (net.minecraft.world.item.ItemStack $$1 : this.items) {
         if ($$1.is($$0)) {
            return true;
         }
      }

      return false;
   }

   public List<net.minecraft.world.item.ItemStack> getItems() {
      return Lists.transform(this.items, net.minecraft.world.item.ItemStack::copy);
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof ChargedProjectiles $$1 && net.minecraft.world.item.ItemStack.listMatches(this.items, $$1.items);
   }

   @Override
   public int hashCode() {
      return net.minecraft.world.item.ItemStack.hashStackList(this.items);
   }

   @Override
   public String toString() {
      return "ChargedProjectiles[items=" + this.items + "]";
   }

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      net.minecraft.world.item.ItemStack $$4 = null;
      int $$5 = 0;

      for (net.minecraft.world.item.ItemStack $$6 : this.items) {
         if ($$4 == null) {
            $$4 = $$6;
            $$5 = 1;
         } else if (net.minecraft.world.item.ItemStack.matches($$4, $$6)) {
            $$5++;
         } else {
            addProjectileTooltip($$0, $$1, $$4, $$5);
            $$4 = $$6;
            $$5 = 1;
         }
      }

      if ($$4 != null) {
         addProjectileTooltip($$0, $$1, $$4, $$5);
      }
   }

   private static void addProjectileTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.ItemStack $$2, int $$3
   ) {
      if ($$3 == 1) {
         $$1.accept(Component.translatable("item.minecraft.crossbow.projectile.single", new Object[]{$$2.getDisplayName()}));
      } else {
         $$1.accept(Component.translatable("item.minecraft.crossbow.projectile.multiple", new Object[]{$$3, $$2.getDisplayName()}));
      }

      TooltipDisplay $$4 = (TooltipDisplay)$$2.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
      $$2.addDetailsToTooltip(
         $$0, $$4, null, net.minecraft.world.item.TooltipFlag.NORMAL, $$1x -> $$1.accept(Component.literal("  ").append($$1x).withStyle(ChatFormatting.GRAY))
      );
   }
}
