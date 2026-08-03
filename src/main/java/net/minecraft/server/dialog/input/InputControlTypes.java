package net.minecraft.server.dialog.input;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class InputControlTypes {
   public static MapCodec<? extends InputControl> bootstrap(Registry<MapCodec<? extends InputControl>> $$0) {
      Registry.register($$0, Identifier.withDefaultNamespace("boolean"), BooleanInput.MAP_CODEC);
      Registry.register($$0, Identifier.withDefaultNamespace("number_range"), NumberRangeInput.MAP_CODEC);
      Registry.register($$0, Identifier.withDefaultNamespace("single_option"), SingleOptionInput.MAP_CODEC);
      return (MapCodec<? extends InputControl>)Registry.register($$0, Identifier.withDefaultNamespace("text"), TextInput.MAP_CODEC);
   }
}
