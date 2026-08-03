package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;

public record TrimPattern(Identifier assetId, Component description, boolean decal) {
   public static final Codec<TrimPattern> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Identifier.CODEC.fieldOf("asset_id").forGetter(TrimPattern::assetId),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(TrimPattern::description),
            Codec.BOOL.fieldOf("decal").orElse(false).forGetter(TrimPattern::decal)
         )
         .apply($$0, TrimPattern::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, TrimPattern> DIRECT_STREAM_CODEC = StreamCodec.composite(
      Identifier.STREAM_CODEC,
      TrimPattern::assetId,
      ComponentSerialization.STREAM_CODEC,
      TrimPattern::description,
      ByteBufCodecs.BOOL,
      TrimPattern::decal,
      TrimPattern::new
   );
   public static final Codec<Holder<TrimPattern>> CODEC = RegistryFileCodec.create(Registries.TRIM_PATTERN, DIRECT_CODEC);
   public static final StreamCodec<RegistryFriendlyByteBuf, Holder<TrimPattern>> STREAM_CODEC = ByteBufCodecs.holder(
      Registries.TRIM_PATTERN, DIRECT_STREAM_CODEC
   );

   public Component copyWithStyle(Holder<TrimMaterial> $$0) {
      return this.description.copy().withStyle(((TrimMaterial)$$0.value()).description().getStyle());
   }
}
