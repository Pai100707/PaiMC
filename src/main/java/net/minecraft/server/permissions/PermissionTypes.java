package net.minecraft.server.permissions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class PermissionTypes {
   public static MapCodec<? extends Permission> bootstrap(Registry<MapCodec<? extends Permission>> $$0) {
      Registry.register($$0, Identifier.withDefaultNamespace("atom"), Permission.Atom.MAP_CODEC);
      return (MapCodec<? extends Permission>)Registry.register($$0, Identifier.withDefaultNamespace("command_level"), Permission.HasCommandLevel.MAP_CODEC);
   }
}
