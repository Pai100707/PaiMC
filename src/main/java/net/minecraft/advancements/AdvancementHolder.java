package net.minecraft.advancements;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record AdvancementHolder(Identifier id, net.minecraft.advancements.Advancement value) {
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.advancements.AdvancementHolder> STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC,
      net.minecraft.advancements.AdvancementHolder::id,
      net.minecraft.advancements.Advancement.STREAM_CODEC,
      net.minecraft.advancements.AdvancementHolder::value,
      net.minecraft.advancements.AdvancementHolder::new
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, List<net.minecraft.advancements.AdvancementHolder>> LIST_STREAM_CODEC = STREAM_CODEC.apply(
      ByteBufCodecs.list()
   );

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.advancements.AdvancementHolder $$1 && this.id.equals($$1.id);
   }

   @Override
   public int hashCode() {
      return this.id.hashCode();
   }

   @Override
   public String toString() {
      return this.id.toString();
   }
}
