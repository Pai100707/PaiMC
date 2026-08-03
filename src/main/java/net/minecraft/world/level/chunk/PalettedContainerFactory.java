package net.minecraft.world.level.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record PalettedContainerFactory(
   Strategy<BlockState> blockStatesStrategy,
   BlockState defaultBlockState,
   Codec<PalettedContainer<BlockState>> blockStatesContainerCodec,
   Strategy<Holder<Biome>> biomeStrategy,
   Holder<Biome> defaultBiome,
   Codec<PalettedContainerRO<Holder<Biome>>> biomeContainerCodec
) {
   public static PalettedContainerFactory create(RegistryAccess $$0) {
      Strategy<BlockState> $$1 = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
      BlockState $$2 = Blocks.AIR.defaultBlockState();
      Registry<Biome> $$3 = $$0.lookupOrThrow(Registries.BIOME);
      Strategy<Holder<Biome>> $$4 = Strategy.createForBiomes($$3.asHolderIdMap());
      Reference<Biome> $$5 = $$3.getOrThrow(Biomes.PLAINS);
      return new PalettedContainerFactory(
         $$1, $$2, PalettedContainer.codecRW(BlockState.CODEC, $$1, $$2), $$4, $$5, PalettedContainer.codecRO($$3.holderByNameCodec(), $$4, $$5)
      );
   }

   public PalettedContainer<BlockState> createForBlockStates() {
      return new PalettedContainer<>(this.defaultBlockState, this.blockStatesStrategy);
   }

   public PalettedContainer<Holder<Biome>> createForBiomes() {
      return new PalettedContainer<>(this.defaultBiome, this.biomeStrategy);
   }
}
