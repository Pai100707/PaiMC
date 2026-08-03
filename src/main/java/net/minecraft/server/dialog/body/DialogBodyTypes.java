package net.minecraft.server.dialog.body;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class DialogBodyTypes {
   public static MapCodec<? extends DialogBody> bootstrap(Registry<MapCodec<? extends DialogBody>> $$0) {
      Registry.register($$0, Identifier.withDefaultNamespace("item"), ItemBody.MAP_CODEC);
      return (MapCodec<? extends DialogBody>)Registry.register($$0, Identifier.withDefaultNamespace("plain_message"), PlainMessage.MAP_CODEC);
   }
}
