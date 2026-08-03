package net.minecraft.server;

import java.util.List;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.BuiltInRegistries;

public enum RegistryLayer {
   STATIC,
   WORLDGEN,
   DIMENSIONS,
   RELOADABLE;

   private static final List<net.minecraft.server.RegistryLayer> VALUES = List.of(values());
   private static final Frozen STATIC_ACCESS = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

   public static LayeredRegistryAccess<net.minecraft.server.RegistryLayer> createRegistryAccess() {
      return new LayeredRegistryAccess(VALUES).replaceFrom(STATIC, new Frozen[]{STATIC_ACCESS});
   }
}
