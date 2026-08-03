package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(
   HolderSet<Block> canGrowThrough,
   HolderSet<Block> muddyRootsIn,
   BlockStateProvider muddyRootsProvider,
   int maxRootWidth,
   int maxRootLength,
   float randomSkewChance
) {
   public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter($$0x -> $$0x.canGrowThrough),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("muddy_roots_in").forGetter($$0x -> $$0x.muddyRootsIn),
            BlockStateProvider.CODEC.fieldOf("muddy_roots_provider").forGetter($$0x -> $$0x.muddyRootsProvider),
            Codec.intRange(1, 12).fieldOf("max_root_width").forGetter($$0x -> $$0x.maxRootWidth),
            Codec.intRange(1, 64).fieldOf("max_root_length").forGetter($$0x -> $$0x.maxRootLength),
            Codec.floatRange(0.0F, 1.0F).fieldOf("random_skew_chance").forGetter($$0x -> $$0x.randomSkewChance)
         )
         .apply($$0, MangroveRootPlacement::new)
   );
}
