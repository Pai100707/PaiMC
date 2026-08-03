package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant;

public record Bees(List<Occupant> bees) implements TooltipProvider {
   public static final Codec<Bees> CODEC = Occupant.LIST_CODEC.xmap(Bees::new, Bees::bees);
   public static final StreamCodec<RegistryFriendlyByteBuf, Bees> STREAM_CODEC = Occupant.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Bees::new, Bees::bees);
   public static final Bees EMPTY = new Bees(List.of());

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      $$1.accept(Component.translatable("container.beehive.bees", new Object[]{this.bees.size(), 3}).withStyle(ChatFormatting.GRAY));
   }
}
