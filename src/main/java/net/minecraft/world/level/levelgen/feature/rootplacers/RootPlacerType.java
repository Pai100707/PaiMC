package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class RootPlacerType<P extends RootPlacer> {
   public static final RootPlacerType<MangroveRootPlacer> MANGROVE_ROOT_PLACER = register("mangrove_root_placer", MangroveRootPlacer.CODEC);
   private final MapCodec<P> codec;

   private static <P extends RootPlacer> RootPlacerType<P> register(String $$0, MapCodec<P> $$1) {
      return (RootPlacerType<P>)Registry.register(BuiltInRegistries.ROOT_PLACER_TYPE, $$0, new RootPlacerType($$1));
   }

   private RootPlacerType(MapCodec<P> $$0) {
      this.codec = $$0;
   }

   public MapCodec<P> codec() {
      return this.codec;
   }
}
