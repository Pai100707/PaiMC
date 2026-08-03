package net.minecraft.server.dialog.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class ActionTypes {
   public static MapCodec<? extends Action> bootstrap(Registry<MapCodec<? extends Action>> $$0) {
      StaticAction.WRAPPED_CODECS.forEach(($$1, $$2) -> Registry.register($$0, Identifier.withDefaultNamespace($$1.getSerializedName()), $$2));
      Registry.register($$0, Identifier.withDefaultNamespace("dynamic/run_command"), CommandTemplate.MAP_CODEC);
      return (MapCodec<? extends Action>)Registry.register($$0, Identifier.withDefaultNamespace("dynamic/custom"), CustomAll.MAP_CODEC);
   }
}
