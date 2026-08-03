package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public record ConfiguredWorldCarver<WC extends CarverConfiguration>(WorldCarver<WC> worldCarver, WC config) {
   public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = BuiltInRegistries.CARVER
      .byNameCodec()
      .dispatch($$0 -> $$0.worldCarver, WorldCarver::configuredCodec);
   public static final Codec<Holder<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registries.CONFIGURED_CARVER, DIRECT_CODEC);
   public static final Codec<HolderSet<ConfiguredWorldCarver<?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.CONFIGURED_CARVER, DIRECT_CODEC);

   public boolean isStartChunk(RandomSource $$0) {
      return this.worldCarver.isStartChunk(this.config, $$0);
   }

   public boolean carve(
      CarvingContext $$0,
      ChunkAccess $$1,
      Function<BlockPos, Holder<Biome>> $$2,
      RandomSource $$3,
      Aquifer $$4,
      net.minecraft.world.level.ChunkPos $$5,
      CarvingMask $$6
   ) {
      return SharedConstants.debugVoidTerrain($$1.getPos()) ? false : this.worldCarver.carve($$0, this.config, $$1, $$2, $$3, $$4, $$5, $$6);
   }
}
