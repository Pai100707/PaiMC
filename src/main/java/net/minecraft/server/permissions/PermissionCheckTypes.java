package net.minecraft.server.permissions;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class PermissionCheckTypes {
   public static MapCodec<? extends PermissionCheck> bootstrap(Registry<MapCodec<? extends PermissionCheck>> $$0) {
      Registry.register($$0, Identifier.withDefaultNamespace("always_pass"), PermissionCheck.AlwaysPass.MAP_CODEC);
      return (MapCodec<? extends PermissionCheck>)Registry.register($$0, Identifier.withDefaultNamespace("require"), PermissionCheck.Require.MAP_CODEC);
   }
}
