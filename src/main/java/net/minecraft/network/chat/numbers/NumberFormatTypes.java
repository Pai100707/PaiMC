package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NumberFormatTypes {
   public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE
      .byNameCodec()
      .dispatchMap(NumberFormat::type, NumberFormatType::mapCodec);
   public static final Codec<NumberFormat> CODEC = MAP_CODEC.codec();
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, NumberFormat> STREAM_CODEC = ByteBufCodecs.<NumberFormatType<? extends NumberFormat>>registry(
         Registries.NUMBER_FORMAT_TYPE
      )
      .dispatch(NumberFormat::type, NumberFormatType::streamCodec);
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Optional<NumberFormat>> OPTIONAL_STREAM_CODEC = STREAM_CODEC.apply(
      ByteBufCodecs::optional
   );

   public static NumberFormatType<?> bootstrap(Registry<NumberFormatType<?>> $$0) {
      Registry.register($$0, "blank", BlankFormat.TYPE);
      Registry.register($$0, "styled", StyledFormat.TYPE);
      return (NumberFormatType<?>)Registry.register($$0, "fixed", FixedFormat.TYPE);
   }
}
